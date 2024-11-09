package sk.stuba.pks.library

import io.ktor.network.sockets.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import sk.stuba.pks.old.dto.Packet
import java.net.BindException
import java.net.SocketException
import java.util.*
import java.util.concurrent.ConcurrentLinkedDeque
import kotlin.time.Duration.Companion.seconds

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
        var isSent = false
        val data = packet.bytes
        withTimeout(30.seconds) {
            while (!isSent) {
                try {
                    val addrs = InetSocketAddress(serverAddress, serverPort)
                    val byteReadPacket = ByteReadPacket(data)
                    val datagramPacket = Datagram(byteReadPacket, addrs)
                    socket.send(datagramPacket)
                    isSent = true
                } catch (e: BindException) {
                    println("Cant bind, retrying")
                    delay(3.seconds)
                } catch (e: SocketException) {
                    println("Cant bind, retrying")
                    delay(3.seconds)
                }
            }
            println("Sent packet $sent")
        }
    }
}
