package sk.stuba.pks.library.service

import io.ktor.network.sockets.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import org.reflections.Reflections.log
import sk.stuba.pks.library.dto.Packet
import sk.stuba.pks.library.dto.PacketBuilder

class PacketReceiver(
    private val socket: BoundDatagramSocket,
) {
    fun startReceivingPackets(): Flow<Packet> =
        callbackFlow {
            withContext(Dispatchers.IO) {
                try {
                    while (true) {
                        try {
                            val datagram = socket.receive()
                            val packet = PacketBuilder.getPacketFromBytes(datagram.packet.readBytes())
                            packet?.let {
                                trySend(it).isSuccess
                            }
                        } catch (e: CancellationException) {
                            close(e)
                            break
                        } catch (e: Exception) {
                            log.info("Some error occurred: trying to reconnect")
                            delay(1000)
                        }
                    }
                } catch (e: Exception) {
                    close(e)
                }
            }
            awaitClose {
            }
        }
}
