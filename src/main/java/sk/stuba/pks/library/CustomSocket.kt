package sk.stuba.pks.library

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import sk.stuba.pks.old.dto.Packet
import sk.stuba.pks.old.dto.PacketBuilder
import sk.stuba.pks.old.model.Message
import sk.stuba.pks.old.model.SynMessage
import sk.stuba.pks.old.service.PacketReceiveListener
import sk.stuba.pks.old.service.mapping.JsonService
import sk.stuba.pks.old.util.IpUtil
import sk.stuba.pks.old.util.PacketUtils
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class CustomSocket(
    private val port: String,
) {
    val address = InetSocketAddress("0.0.0.0", port.toInt()) // Use 0.0.0.0 to bind to all available interfaces
    val myAddress = IpUtil.getIp()!!
    private val udpSocket = aSocket(SelectorManager(Dispatchers.IO)).udp().bind(address)
    private var currentSequenceNumber = ByteArray(4).apply { fill(0x00) }
    private lateinit var sessionId: ByteArray
    private lateinit var serverAddress: String
    var serverPort: Int = 0
        private set
        get() {
            if (field == 0) {
                throw IllegalStateException("Server port is not set")
            }
            return field
        }
    private lateinit var packetSender: PacketSender
    private lateinit var packetFlow: Flow<Packet>

    private val isKeepAliveReceived = AtomicBoolean(true)
    private val unsuccessfulKeepAliveCount = AtomicInteger(0)

    private val unconfirmed: MutableMap<Int, Pair<Packet, Long>> = ConcurrentHashMap()

    val packetListeners: MutableList<PacketReceiveListener> = mutableListOf()

    suspend fun connect(
        serverAddress: String,
        serverPort: Int,
    ): Boolean {
        generateSessionId()
        this.serverAddress = serverAddress
        this.serverPort = serverPort

        sendSyn()
        receiveSynAckMessage()
        sendAck()
        return true
    }

    private suspend fun sendAck() {
        val packet = PacketBuilder.ackPacket(sessionId, currentSequenceNumber)
        sendPacket(packet)
        currentSequenceNumber = PacketUtils.incrementSequenceNumber(currentSequenceNumber)
    }

    private suspend fun receiveSynAckMessage() {
        var packet: Packet
        withTimeout(30_000) {
            packet = receiveMessage()
            while (!packet.isSynAck) {
                packet = receiveMessage()
            }
        }
        currentSequenceNumber = PacketUtils.incrementSequenceNumber(currentSequenceNumber)
        sessionId = packet.sessionId
    }

    private fun generateSessionId() {
        sessionId = ByteArray(4).map { ThreadLocalRandom.current().nextInt(0, 255).toByte() }.toByteArray()
    }

    suspend fun sendSyn() {
        val packet = PacketBuilder.synPacket(sessionId, currentSequenceNumber, port.toInt(), myAddress)
        sendPacket(packet)
    }

    suspend fun waitConnection(): String {
        println("Receiving")
        val packet = receiveSyncMessage()
        val remoteAddress = (JsonService.fromPayload(packet.payload) as SynMessage).address
        sendSynAck()
        receiveAckMessage()

        return remoteAddress
    }

    private suspend fun receiveAckMessage() {
        var packet: Packet
        withTimeout(30_000) {
            packet = receiveMessage()
            while (!packet.isAck) {
                packet = receiveMessage()
            }
        }
    }

    private suspend fun sendSynAck() {
        val packet = PacketBuilder.synAckPacket(sessionId, currentSequenceNumber)
        sendPacket(packet)
        currentSequenceNumber = PacketUtils.incrementSequenceNumber(currentSequenceNumber)
    }

    private suspend fun receiveSyncMessage(): Packet {
        println("Receiving SYN message")
        var packet: Packet
        withTimeout(30_000) {
            packet = receiveMessage()
            while (!packet.isSyn) {
                println("Received packet is not SYN")
                packet = receiveMessage()
            }
        }

        val message: Message = JsonService.fromPayload(packet.payload)
        check(message is SynMessage) { "First message is not SYN message" }

        serverAddress = message.address
        serverPort = message.port
        currentSequenceNumber = packet.sequenceNumber
        currentSequenceNumber = PacketUtils.incrementSequenceNumber(currentSequenceNumber)
        sessionId = packet.sessionId
        return packet
    }

    private suspend fun receiveMessage(): Packet {
        val packet = udpSocket.receive()
        val payload = packet.packet.readBytes()
        return PacketBuilder.getPacketFromBytes(payload)
    }

    suspend fun sendPacket(packet: Packet) {
        val data = packet.bytes

        val addrs = InetSocketAddress(serverAddress, serverPort)
        val byteReadPacket = ByteReadPacket(data)
        val datagramPacket = Datagram(byteReadPacket, addrs)
        udpSocket.send(datagramPacket)
    }

    fun sendMessage(message: String) {
        val messageBytes = message.toByteArray()
        val messagePackets = messageBytes.asSequence().chunked(1024)
        val totalPackets = messagePackets.count()
        val localMessageIdHash = messagePackets.hashCode()
        messagePackets.forEachIndexed { index, packet ->
            val simpleMessage =
                // language=JSON
                """
                {
                    "type": "simple",
                    "numberOfPackets": "$totalPackets",
                    "message": "${String(packet.toByteArray())}",
                    "localMessageId": "$localMessageIdHash",
                    "localMessageOffset": "$index"
                }
                """.trimIndent()
            val packetToSend = prepareMessagePacket(simpleMessage)
            packetSender.addPacket(packetToSend)
            val seqNumber = PacketUtils.byteArrayToInt(packetToSend.sequenceNumber)
            unconfirmed[seqNumber] = packetToSend to System.currentTimeMillis()
            println("size is ${unconfirmed.size}")
        }
    }

    private fun prepareMessagePacket(simpleMessage: String): Packet {
        currentSequenceNumber = PacketUtils.incrementSequenceNumber(currentSequenceNumber)
        val payloadLen = PacketUtils.intToByteArray(simpleMessage.toByteArray().size)
        val packet =
            PacketBuilder()
                .setSessionId(sessionId)
                .setSequenceNumber(currentSequenceNumber)
                .setPayload(simpleMessage.toByteArray())
                .setPayloadLength(payloadLen)
                .setPayloadType(0b00)
                .setAckFlag(0b00)
                .build()
        return packet
    }

    fun startSending() {
        packetSender = PacketSender(udpSocket, serverAddress, serverPort)
        packetSender.startSendingPackets()
        val packetReceiver = PacketReceiver(udpSocket)
        packetFlow = packetReceiver.startReceivingPackets()
        handleReceivedPackets()
        resender()
        sendKeepAlive()
    }

    private fun handleReceivedPackets() {
        CoroutineScope(Dispatchers.Default).launch {
            var received = 0
            packetFlow.collect { packet ->
                launch {
                    if (packet.isCorrupt) return@launch

                    if (packet.isAck && !packet.isKeepAlive) {
                        val sequenceNumber = PacketUtils.byteArrayToInt(packet.sequenceNumber)
                        unconfirmed.remove(sequenceNumber)
                        received++
                        println("Received $received packets")
                    }

                    if (packet.isData && !packet.isAck) {
                        packetSender.sendPacket(PacketBuilder.ackPacket(sessionId, packet.sequenceNumber))
                        packetListeners.forEach { it.onPacketReceived(packet) }
                    }

                    if (packet.isKeepAlive && !packet.isAck) {
                        packetSender.sendPacket(PacketBuilder.keepAliveAckPacket(sessionId, packet.sequenceNumber))
                    }

                    if (packet.isKeepAlive && packet.isAck) {
                        isKeepAliveReceived.set(true)
                        unsuccessfulKeepAliveCount.set(0)
                    }
                }
            }
        }
    }

    private fun resender() {
        CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                if (unconfirmed.isEmpty()) {
                    delay(100)
                    continue
                }
                println("we have ${unconfirmed.size} unconfirmed packets")
                unconfirmed.asSequence().take(10000).forEach { (seqNumber, packet) ->
                    if (System.currentTimeMillis() - packet.second > 500) {
                        packetSender.addPacketToBeginning(packet.first)
                        unconfirmed[seqNumber] = packet.first to System.currentTimeMillis()
                        println("Resending packet with seqNumber $seqNumber")
                    }
                }
                delay(20)
            }
        }
    }

    private fun sendKeepAlive() {
        CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                if (!isKeepAliveReceived.get()) {
                    unsuccessfulKeepAliveCount.incrementAndGet()
                    if (unsuccessfulKeepAliveCount.get() > 3) {
                        println("Connection lost")
                        break
                    }
                } else {
                    unsuccessfulKeepAliveCount.set(0)
                }
                isKeepAliveReceived.set(false)
                val packet = PacketBuilder.keepAlivePacket(sessionId, currentSequenceNumber)
                sendPacket(packet)
                currentSequenceNumber = PacketUtils.incrementSequenceNumber(currentSequenceNumber)
                Thread.sleep(5000)
            }
        }
    }

    fun sendFile(filePath: String) {
        val fileBytes = Files.readAllBytes(Paths.get(filePath))
        val filePackets = fileBytes.asSequence().chunked(750)
        val totalPackets = filePackets.count()
        val localMessageIdHash = filePackets.hashCode()
        runBlocking {
            filePackets
                .mapIndexed { index, packet ->
                    async(Dispatchers.Default) {
                        val base64Payload = Base64.getEncoder().encodeToString(packet.toByteArray())
                        val simpleMessage =
                            // language=JSON
                            """
                            {
                                "type": "file",
                                "fileName": "${File(filePath).name}",
                                "numberOfPackets": "$totalPackets",
                                "payload": "$base64Payload",
                                "localMessageId": "$localMessageIdHash",
                                "localMessageOffset": "$index"
                            }
                            """.trimIndent()

                        val packetToSend = prepareMessagePacket(simpleMessage)
                        packetSender.addPacket(packetToSend)
                        val seqNumber = PacketUtils.byteArrayToInt(packetToSend.sequenceNumber)
                        unconfirmed[seqNumber] = packetToSend to System.currentTimeMillis()
                    }
                }.toList()
                .awaitAll()
        }
    }
}
