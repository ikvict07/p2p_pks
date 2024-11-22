package sk.stuba.pks.library.service

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.reflections.Reflections.log
import sk.stuba.pks.library.dto.Packet
import sk.stuba.pks.library.dto.PacketBuilder
import sk.stuba.pks.library.enums.StaticDefinition
import sk.stuba.pks.library.model.Message
import sk.stuba.pks.library.model.SynMessage
import sk.stuba.pks.library.service.mapping.JsonService
import sk.stuba.pks.library.util.IpUtil
import sk.stuba.pks.library.util.PacketUtils
import sk.stuba.pks.starter.configuration.SocketConfigurationProperties
import java.io.File
import java.io.InputStream
import java.net.BindException
import java.net.SocketException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.min

class CustomSocket(
    private val port: String,
    private val socketConfigurationProperties: SocketConfigurationProperties,
) {
    private val isMyFin = AtomicBoolean(false)
    private val isRemoteFin = AtomicBoolean(false)
    val isClosed = AtomicBoolean(false)

    private val address = InetSocketAddress("0.0.0.0", port.toInt()) // Use 0.0.0.0 to bind to all available interfaces
    private val myAddress = IpUtil.getIp()!!
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

    fun isClosedByMe() = isMyFin.get()

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

    suspend fun waitConnection(): String {
        val packet = receiveSyncMessage()
        val remoteAddress = (JsonService.fromPayload(packet.payload) as SynMessage).address
        sendSynAck()
        receiveAckMessage()

        return remoteAddress
    }

    private suspend fun sendAck() {
        val packet = PacketBuilder.ackPacket(sessionId, currentSequenceNumber)
        sendPacket(packet)
        currentSequenceNumber = PacketUtils.incrementSequenceNumber(currentSequenceNumber)
    }

    private suspend fun receiveSynAckMessage() {
        var packet: Packet
        withTimeout(socketConfigurationProperties.connectionTimeoutMs) {
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

    private suspend fun sendSyn() {
        val packet = PacketBuilder.synPacket(sessionId, currentSequenceNumber, port.toInt(), myAddress)
        sendPacket(packet)
    }

    private suspend fun receiveAckMessage() {
        var packet: Packet
        withTimeout(socketConfigurationProperties.connectionTimeoutMs) {
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
        log.info("Receiving SYN message")
        var packet: Packet
        withTimeout(socketConfigurationProperties.connectionTimeoutMs) {
            packet = receiveMessage()
            while (!packet.isSyn) {
                log.error("Received packet is not SYN")
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

    private suspend fun sendPacket(packet: Packet) {
        val data = packet.bytes
        var isSent = false
        withTimeout(socketConfigurationProperties.connectionTimeoutMs) {
            while (!isSent) {
                try {
                    val addrs = InetSocketAddress(serverAddress, serverPort)
                    val byteReadPacket = ByteReadPacket(data)
                    val datagramPacket = Datagram(byteReadPacket, addrs)
                    udpSocket.send(datagramPacket)
                    isSent = true
                } catch (e: BindException) {
                    log.error("Cant bind, retrying")
                    delay(socketConfigurationProperties.retryToConnectEveryMs)
                } catch (e: SocketException) {
                    log.error("Cant bind, retrying")
                    delay(socketConfigurationProperties.retryToConnectEveryMs)
                }
            }
        }
    }

    fun sendMessage(
        message: String,
        maxPacketSize: Long,
    ) {
        val messageBytes = message.toByteArray()
        val messagePackets =
            messageBytes.asSequence().chunked(
                min(
                    (StaticDefinition.MESSAGE_MAX_SIZE.value - 669),
                    maxPacketSize.toInt(),
                ),
            )
        val totalPackets = messagePackets.count()
        val localMessageIdHash = messagePackets.hashCode()
        messagePackets.forEachIndexed { index, packet ->
            val base64Payload = Base64.getEncoder().encodeToString(packet.toByteArray())
            val simpleMessage =
                // language=JSON
                """
                {
                    "type": "simple",
                    "numberOfPackets": "$totalPackets",
                    "message": "$base64Payload",
                    "localMessageId": "$localMessageIdHash",
                    "localMessageOffset": "$index"
                }
                """.trimIndent()
            val packetToSend = prepareMessagePacket(simpleMessage)
            packetSender.addPacket(packetToSend)
            val seqNumber = PacketUtils.byteArrayToInt(packetToSend.sequenceNumber)
            unconfirmed[seqNumber] = packetToSend to System.currentTimeMillis()
        }
    }

    fun sendCorruptedMessage(
        message: String,
        maxPacketSize: Long,
    ) {
        val messageBytes = message.toByteArray()
        val messagePackets =
            messageBytes.asSequence().chunked(
                min(
                    (StaticDefinition.MESSAGE_MAX_SIZE.value - 669),
                    maxPacketSize.toInt(),
                ),
            )
        val totalPackets = messagePackets.count()
        val localMessageIdHash = messagePackets.hashCode()
        messagePackets.forEachIndexed { index, packet ->
            val base64Payload = Base64.getEncoder().encodeToString(packet.toByteArray())
            val simpleMessage =
                // language=JSON
                """
                {
                    "type": "simple",
                    "numberOfPackets": "$totalPackets",
                    "message": "$base64Payload",
                    "localMessageId": "$localMessageIdHash",
                    "localMessageOffset": "$index"
                }
                """.trimIndent()
            val packetToSend = prepareMessagePacket(simpleMessage)
            val corrupted =
                PacketBuilder()
                    .setSessionId(packetToSend.sessionId)
                    .setSequenceNumber(packetToSend.sequenceNumber)
                    .setPayload(packetToSend.payload)
                    .setPayloadLength(packetToSend.payloadLength)
                    .setPayloadType(packetToSend.payloadType)
                    .setAckFlag(packetToSend.ackFlag)
                    .setChecksum(PacketUtils.intToByteArray(0))
                    .build()
            packetSender.addPacket(corrupted)
            val seqNumber = PacketUtils.byteArrayToInt(packetToSend.sequenceNumber)
            unconfirmed[seqNumber] = packetToSend to System.currentTimeMillis()
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

    suspend fun startSending() {
        coroutineScope {
            packetSender =
                PacketSender(
                    udpSocket,
                    serverAddress,
                    serverPort,
                    socketConfigurationProperties.connectionTimeoutMs,
                    socketConfigurationProperties.retryToConnectEveryMs,
                )
            launch(Dispatchers.IO) { packetSender.startSendingPackets() }

            val packetReceiver = PacketReceiver(udpSocket)
            packetFlow = packetReceiver.startReceivingPackets()

            launch(Dispatchers.IO) { handleReceivedPackets() }
            launch(Dispatchers.IO) { resender() }
            launch(Dispatchers.IO) { sendKeepAlive() }
        }
    }

    private suspend fun handleReceivedPackets() {
        var received = 0
        packetFlow.collect { packet ->
            if (packet.isCorrupt) return@collect
            if (!packet.sessionId.contentEquals(sessionId)) return@collect

            log.info("Payload size is: ${packet.payload.size}")
            if (packet.isAck && !packet.isKeepAlive) {
                val sequenceNumber = PacketUtils.byteArrayToInt(packet.sequenceNumber)
                unconfirmed.remove(sequenceNumber)
                received++
                log.info("Received $received packets")
            }

            if (packet.isData && !packet.isAck) {
                packetSender.sendPacket(PacketBuilder.ackPacket(sessionId, packet.sequenceNumber))
                packetListeners.forEach { it.onPacketReceived(packet, port) }
            }

            if (packet.isKeepAlive && !packet.isAck) {
                packetSender.sendPacket(PacketBuilder.keepAliveAckPacket(sessionId, packet.sequenceNumber))
            }

            if (packet.isKeepAlive && packet.isAck) {
                isKeepAliveReceived.set(true)
                unsuccessfulKeepAliveCount.set(0)
            }
            if (packet.isFin && !packet.isAck) {
                log.info("Received FIN")
                isRemoteFin.set(true)
                val confirmFin = PacketBuilder.finAckPacket(sessionId, packet.sequenceNumber)
                packetSender.sendPacket(confirmFin)
                if (isMyFin.get() && isRemoteFin.get()) {
                    isClosed.set(true)
                    log.error("This connection is closed")
                    throw CancellationException("Connection is closed")
                }
            }
            if (packet.isFin && packet.isAck) {
                log.info("Received FIN ACK")
                unconfirmed.clear()
                packetSender.clearQueue()
                isMyFin.set(true)
                if (isMyFin.get() && isRemoteFin.get()) {
                    isClosed.set(true)
                    log.error("This connection is closed")
                    throw CancellationException("Connection is closed")
                }
            }
        }
    }

    fun sendFin() {
        val packet = PacketBuilder.finPacket(sessionId, currentSequenceNumber)
        packetSender.addPacket(packet)
        val seqNumber = PacketUtils.byteArrayToInt(packet.sequenceNumber)
        if (!isRemoteFin.get()) {
            unconfirmed[seqNumber] = packet to System.currentTimeMillis()
        }
    }

    private suspend fun resender() {
        while (true) {
            if (unconfirmed.isEmpty()) {
                delay(socketConfigurationProperties.messageResendingFrequencyMs)
                continue
            }
            log.trace("we have ${unconfirmed.size} unconfirmed packets")
            unconfirmed.asSequence().take(500).forEach { (seqNumber, packet) ->
                if (System.currentTimeMillis() - packet.second > socketConfigurationProperties.messageResendingConfirmationTimeMs) {
                    packetSender.addPacketToBeginning(packet.first)
                    unconfirmed[seqNumber] = packet.first to System.currentTimeMillis()
                    log.trace("Resending packet with seqNumber $seqNumber")
                }
            }
            delay(socketConfigurationProperties.messageResendingFrequencyMs)
        }
    }

    private suspend fun sendKeepAlive() {
        while (true) {
            if (!isKeepAliveReceived.get()) {
                unsuccessfulKeepAliveCount.incrementAndGet()
                if (unsuccessfulKeepAliveCount.get() > socketConfigurationProperties.attemptsToReconnect) {
                    log.error("Connection lost")
                    break
                }
            } else {
                unsuccessfulKeepAliveCount.set(0)
            }
            isKeepAliveReceived.set(false)
            val packet = PacketBuilder.keepAlivePacket(sessionId, currentSequenceNumber)
            sendPacket(packet)
            currentSequenceNumber = PacketUtils.incrementSequenceNumber(currentSequenceNumber)
            delay(socketConfigurationProperties.keepAliveFrequencyMs)
        }
    }

    fun sendFile(
        filePath: String,
        maxPacketSize: Long,
    ) {
        val filePackets =
            Files.newInputStream(Paths.get(filePath)).use { inputStream ->
                inputStream
                    .chunkSequence(
                        min(
                            StaticDefinition.MESSAGE_MAX_SIZE.value - 669,
                            maxPacketSize.toInt(),
                        ),
                    ).toList()
            }
        val totalPackets = filePackets.count()
        val localMessageIdHash = filePackets.hashCode()
        filePackets.forEachIndexed { index, packet ->
            val base64Payload = Base64.getEncoder().encodeToString(packet)
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
    }

    private fun InputStream.chunkSequence(chunkSize: Int) =
        sequence {
            val buffer = ByteArray(chunkSize)
            var bytesRead: Int
            while (true) {
                bytesRead = this@chunkSequence.read(buffer)
                if (bytesRead <= 0) break
                yield(buffer.copyOf(bytesRead))
            }
        }
}
