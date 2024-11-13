package sk.stuba.pks.library.service

import sk.stuba.pks.library.dto.Packet
import sk.stuba.pks.library.model.FileMessage
import sk.stuba.pks.library.model.SimpleMessage
import sk.stuba.pks.library.service.mapping.JsonService

class PacketReceiveListenerImpl(
    private val messageListeners: List<MessageListener>,
) : PacketReceiveListener {
    private val messageCollectors = mutableMapOf<Int, MessageCollector>()
    private val fileCollectors = mutableMapOf<String, FileCollector>()

    override fun onPacketReceived(packet: Packet?) {
        if (packet == null) return
        val message = JsonService.fromPayload(packet.payload)
        if (message is SimpleMessage) {
            messageCollectors
                .computeIfAbsent(message.localMessageId) {
                    MessageCollector(message.localMessageId, message.numberOfPackets)
                }.addMessage(message)
            if (messageCollectors[message.localMessageId]!!.isComplete()) {
                notifyListenersMessage(messageCollectors[message.localMessageId]!!.getCompleteMessage())
                messageCollectors.remove(message.localMessageId)
            }
        }
        if (message is FileMessage) {
            fileCollectors
                .computeIfAbsent(
                    message.fileName,
                ) { FileCollector(message.fileName, message.numberOfPackets) }
                .addPacket(message)
            if (fileCollectors[message.fileName]!!.isComplete()) {
                notifyListenersFile(message.fileName, fileCollectors[message.fileName]!!.getCompleteFile())
                fileCollectors.remove(message.fileName)
            }
        }
    }

    private fun notifyListenersMessage(message: String) {
        for (listener in messageListeners) {
            listener.onMessageReceive(message)
        }
    }

    private fun notifyListenersFile(
        fileName: String,
        fileContent: ByteArray,
    ) {
        for (listener in messageListeners) {
            listener.onFileReceive(fileName, fileContent)
        }
    }
}
