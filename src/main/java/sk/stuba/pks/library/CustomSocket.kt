package sk.stuba.pks.library

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withTimeout
import sk.stuba.pks.old.dto.Packet
import sk.stuba.pks.old.dto.PacketBuilder
import sk.stuba.pks.old.model.Message
import sk.stuba.pks.old.model.SynMessage
import sk.stuba.pks.old.service.mapping.JsonService
import sk.stuba.pks.old.util.IpUtil
import sk.stuba.pks.old.util.PacketUtils


class CustomSocket(
    private val port: String
) {

    val address = InetSocketAddress("0.0.0.0", port.toInt()) // Use 0.0.0.0 to bind to all available interfaces
    val myAddress = IpUtil.getIp()!!
    private val udpSocket = aSocket(SelectorManager(Dispatchers.IO)).udp().bind(address)
    private var currentSequenceNumber = ByteArray(4).apply { fill(0x00) }
    private lateinit var sessionId: ByteArray
    private lateinit var serverAddress: String
    private var serverPort: Int = 0


    suspend fun waitConnection(): Boolean {
        println("Receiving")
        receiveSyncMessage()
        sendSynAck()
        receiveAckMessage()
        return true
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
        println("faster")
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

    suspend fun getMessage(): Packet {
        var packet = receiveMessage()
        println(packet)
        while (packet.isAck or packet.isSyn or packet.isSynAck or !packet.sessionId.contentEquals(sessionId) or packet.isKeepAlive) {
            if (packet.isKeepAlive) {
                sendPacket(PacketBuilder.keepAliveAckPacket(sessionId, packet.sequenceNumber))
            }
            packet = receiveMessage()
        }

        sendPacket(PacketBuilder.ackPacket(sessionId, packet.sequenceNumber))
        return packet
    }
}