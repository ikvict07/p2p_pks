package sk.stuba.pks.library.service

import io.ktor.util.collections.*
import org.reflections.Reflections.log
import sk.stuba.pks.library.model.FileMessage
import java.io.ByteArrayOutputStream
import java.time.Duration
import java.time.LocalDateTime
import java.time.Period

class FileCollector(
    private val fileName: String,
    totalPackets: Int,
): ByteCounter<FileMessage> (fileName, totalPackets) {
    private val packets = ConcurrentSet<FileMessage>()

    private val startTime = LocalDateTime.now()

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
