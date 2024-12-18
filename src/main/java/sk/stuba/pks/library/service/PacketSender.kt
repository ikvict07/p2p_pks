package sk.stuba.pks.library.service

import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import org.reflections.Reflections.log
import sk.stuba.pks.library.dto.Packet
import java.net.BindException
import java.net.SocketException
import java.util.*
import java.util.concurrent.ConcurrentLinkedDeque

class PacketSender(
    private val socket: BoundDatagramSocket,
    private val serverAddress: String,
    private val serverPort: Int,
    private val connectionTimeoutMs: Long,
    private val reconnectEveryMs: Long,
    private val reconnectMaxAttempts: Long,
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
        var attempts = 0
        withTimeout(connectionTimeoutMs) {
            while (!isSent) {
                try {
                    val addrs = InetSocketAddress(serverAddress, serverPort)
                    val byteReadPacket = ByteReadPacket(data)
                    val datagramPacket = Datagram(byteReadPacket, addrs)
                    socket.send(datagramPacket)
                    isSent = true
                    attempts = 0
                } catch (e: BindException) {
                    if (attempts > reconnectMaxAttempts) {
                        throw CancellationException("Couldn't reconnect")
                    }
                    attempts++
                    log.info("Cant bind, retrying")
                    delay(reconnectEveryMs)
                } catch (e: SocketException) {
                    if (attempts > reconnectMaxAttempts) {
                        throw CancellationException("Couldn't reconnect")
                    }
                    attempts++
                    log.info("Cant bind, retrying")
                    delay(reconnectEveryMs)
                }
            }
            log.trace("Sent packet $sent")
        }
    }

    fun clearQueue() {
        packetQueue.clear()
    }
}
