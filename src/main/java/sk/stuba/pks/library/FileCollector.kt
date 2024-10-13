package sk.stuba.pks.library

import sk.stuba.pks.old.model.FileMessage

class FileCollector(
    val fileName: String,
    val totalPackets: Int,
) {

    private val packets = mutableListOf<FileMessage>()

    fun addPacket(message: FileMessage) {
        packets.add(message)
    }

    fun isComplete(): Boolean {
        return packets.size == totalPackets
    }

    fun getCompleteFile(): ByteArray {
        return (
            packets.sortedBy { it.localMessageOffset }
                .map { it.payload }
                .reduce { acc, bytes -> acc + bytes }
        )
    }

}