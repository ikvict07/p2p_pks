package sk.stuba.pks.library

import io.ktor.network.sockets.*
import io.ktor.utils.io.core.*
import sk.stuba.pks.old.dto.Packet
import java.util.*
import java.util.concurrent.ConcurrentLinkedDeque

class PacketSender(
    private val socket: BoundDatagramSocket,
    private val serverAddress: String,
    private val serverPort: Int,
) {
    private val packetQueue: Deque<Packet> = ConcurrentLinkedDeque()

    suspend fun startSendingPackets() {
        while (true) {
            val packet: Packet? = packetQueue.poll()
            packet?.let {
                sendPacket(it)
            }
        }
    }

    private var sent = 0

    fun addPacket(packet: Packet) {
        packetQueue.add(packet)
    }

    fun addPacketToBeginning(packet: Packet) {
        packetQueue.addFirst(packet)
    }

    suspend fun sendPacket(packet: Packet) {
        sent++
        println("Sent packet $sent")
        val data = packet.bytes
        val addrs = InetSocketAddress(serverAddress, serverPort)
        val byteReadPacket = ByteReadPacket(data)
        val datagramPacket = Datagram(byteReadPacket, addrs)
        socket.send(datagramPacket)
    }
}
