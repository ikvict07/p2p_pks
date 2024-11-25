package sk.stuba.pks.library.service

import io.ktor.util.collections.*
import org.reflections.Reflections.log
import sk.stuba.pks.library.model.FileMessage
import java.io.ByteArrayOutputStream
import java.time.Duration
import java.time.LocalDateTime
import java.time.Period

class FileCollector(
    val fileName: String,
    private val totalPackets: Int,
) {
    private val packets = ConcurrentSet<FileMessage>()

    private val startTime = LocalDateTime.now()
    fun addPacket(message: FileMessage) {
        packets.add(message)
    }

    fun isComplete(): Boolean {
        log.trace("Packets: ${packets.size}, Total: $totalPackets")
        return packets.size == totalPackets
    }

    fun getCompleteFile(): ByteArray {
        log.info("Received $fileName in ${Duration.between(startTime, LocalDateTime.now()).toSeconds()} seconds")
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
