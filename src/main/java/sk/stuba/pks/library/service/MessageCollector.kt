package sk.stuba.pks.library.service

import org.reflections.Reflections.log
import sk.stuba.pks.library.model.SimpleMessage
import java.util.Base64

class MessageCollector(
    private val messageId: Int,
    private val totalMessagesLen: Int,
) {
    private val messages = mutableSetOf<SimpleMessage>()

    fun addMessage(message: SimpleMessage) {
        messages.add(message)
        if (isComplete()) {
            log.info("Message $messageId is complete")
        }
    }

    fun isComplete(): Boolean = messages.size == totalMessagesLen

    fun getCompleteMessage(): String =
        (
            messages
                .sortedBy { it.localMessageOffset }
                .joinToString("") { String(Base64.getDecoder().decode(it.message)) }
        )
}
