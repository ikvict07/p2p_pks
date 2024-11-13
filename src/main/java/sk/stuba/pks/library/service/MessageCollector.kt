package sk.stuba.pks.library.service

import sk.stuba.pks.library.model.SimpleMessage

class MessageCollector(
    private val messageId: Int,
    private val totalMessagesLen: Int,
) {
    private val messages = mutableSetOf<SimpleMessage>()

    fun addMessage(message: SimpleMessage) {
        messages.add(message)
        if (isComplete()) {
            println("Message $messageId is complete")
        }
        println(getCompleteMessage())
    }

    fun isComplete(): Boolean = messages.size == totalMessagesLen

    fun getCompleteMessage(): String =
        (
            messages
                .sortedBy { it.localMessageOffset }
                .joinToString("") { it.message }
        )
}
