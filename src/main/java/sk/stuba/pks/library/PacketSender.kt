package sk.stuba.pks.library

import io.ktor.network.sockets.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import sk.stuba.pks.old.dto.Packet
import java.util.*
import java.util.concurrent.ConcurrentLinkedDeque

class PacketSender (
    val socket: BoundDatagramSocket,
    val serverAddress: String,
    val serverPort: Int
) {

    val packetQueue: Deque<Packet> = ConcurrentLinkedDeque()


    fun startSendingPackets() {
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                val packet: Packet? = packetQueue.poll()
                packet?.let {
                    sendPacket(it)
                }
            }
        }
    }


    fun addPacket(packet: Packet) {
        packetQueue.add(packet)
    }
    fun addPacketToBeginning(packet: Packet) {
        packetQueue.addFirst(packet)
    }

    suspend fun sendPacket(packet: Packet) {
        val data = packet.bytes
        val addrs = InetSocketAddress(serverAddress, serverPort)
        val byteReadPacket = ByteReadPacket(data)
        val datagramPacket = Datagram(byteReadPacket, addrs)
        socket.send(datagramPacket)
    }
}