package sk.stuba.pks.library.service

import sk.stuba.pks.library.model.SimpleMessage
import java.util.*

class MessageCollector(
    messageId: String,
    totalMessagesLen: Int,
) : ByteCounter<SimpleMessage>(messageId, totalMessagesLen) {

    fun getCompleteMessage(): String =
        (
                messages
                    .sortedBy { it.localMessageOffset }
                    .joinToString("") { String(Base64.getDecoder().decode(it.message)) }
                )

}
