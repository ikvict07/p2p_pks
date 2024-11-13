package sk.stuba.pks.library.service

import io.ktor.util.collections.*
import sk.stuba.pks.library.model.FileMessage
import java.io.ByteArrayOutputStream

class FileCollector(
    val fileName: String,
    val totalPackets: Int,
) {
    private val packets = ConcurrentSet<FileMessage>()

    fun addPacket(message: FileMessage) {
        packets.add(message)
    }

    fun isComplete(): Boolean {
        println("Packets: ${packets.size}, Total: $totalPackets")
        return packets.size == totalPackets
    }

    fun getCompleteFile(): ByteArray {
        val result =
            ByteArrayOutputStream().use { buffer ->
                packets
                    .asSequence()
                    .sortedBy { it.localMessageOffset }
                    .forEach { packet ->
                        buffer.write(packet.payload)
                    }
                buffer.toByteArray()
            }
        return result
    }
}
