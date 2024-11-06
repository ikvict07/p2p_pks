package sk.stuba.pks.library

import io.ktor.network.sockets.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import sk.stuba.pks.old.dto.Packet
import sk.stuba.pks.old.dto.PacketBuilder

class PacketReceiver(
    val socket: BoundDatagramSocket,
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
                        } catch (e: Exception) {
                            close(e)
                            break
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
