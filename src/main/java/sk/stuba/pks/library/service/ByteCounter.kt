package sk.stuba.pks.library.service

import org.reflections.Reflections.log
import sk.stuba.pks.library.dto.Packet
import sk.stuba.pks.library.model.Message

abstract class ByteCounter<T : Message>(
    private val messageId: String,
    private val totalMessagesLen: Int,
) {
    val messages = mutableSetOf<T>()


    private var totalNonPayloadData: Double = 0.0
    private var totalData: Double = 0.0

    fun printInfo(message: T, packet: Packet) {
        val headerLen = (packet.bytes.size - packet.payload.size) * 1.0
        val headerPercent = headerLen / packet.bytes.size * 100.0
        val jsonLen = (packet.payload.size - message.payload.size ) * 1.0
        val jsonPercent = jsonLen / packet.bytes.size * 100.0
        val totalNonPayloadDataPercent = (jsonLen + headerLen) / packet.bytes.size * 100.0
        log.info("Header length: $headerLen/${packet.bytes.size}($headerPercent%), Json: $jsonLen/${packet.bytes.size} ($jsonPercent%), totalNonPayloadData: $totalNonPayloadDataPercent%")

        totalNonPayloadData += jsonLen + headerLen
        totalData += packet.bytes.size * 1.0
        if (isComplete()) {
            printCompleteResult()
        }
    }

    fun isComplete(): Boolean = messages.size == totalMessagesLen


    private fun printCompleteResult() {
        log.info("TotalNonPayloadData: ${totalNonPayloadData}/${totalData} ${totalNonPayloadData / totalData * 100.0}%")
    }


    fun addMessage(message: T, packet: Packet) {
        messages.add(message)
        printInfo(message, packet)
        if (isComplete()) {
            log.info("Message $messageId is complete")
        }
    }
}