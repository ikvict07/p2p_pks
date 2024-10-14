package sk.stuba.pks.library

import sk.stuba.pks.old.model.SimpleMessage

class MessageCollector (
    val messageId: Int,
    val totalMessagesLen: Int,
) {
    val messages = mutableListOf<SimpleMessage>()


    fun addMessage(message: SimpleMessage) {
        messages.add(message)
        if (isComplete()) {
            println("Message $messageId is complete")
        }
        println(getCompleteMessage())
    }

    fun isComplete(): Boolean {
        return messages.size == totalMessagesLen
    }

    fun getCompleteMessage(): String {
        return (
            messages.sortedBy { it.localMessageOffset }
                .joinToString("") { it.message }
        )
    }
}