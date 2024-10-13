package sk.stuba.pks.starter

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import sk.stuba.pks.library.CustomSocket
import sk.stuba.pks.library.FileCollector
import sk.stuba.pks.library.MessageCollector
import sk.stuba.pks.old.model.FileMessage
import sk.stuba.pks.old.model.SimpleMessage
import sk.stuba.pks.old.service.mapping.JsonService


class SocketListener(
    private val port: String,
    private val messageListeners: List<MessageListener>,
) {

    private val socket = CustomSocket(port)
    private val messageCollectors = mutableMapOf<Int, MessageCollector>()
    private val fileCollectors = mutableMapOf<String, FileCollector>()
    fun init() {
        println("SocketListener initialized on port $port")
        startListening()
    }

    fun startListening() {
        println("Listening on port HHH $port")
        CoroutineScope(Dispatchers.IO).launch {
            val result = async {
                try {
                    println("Trying")
                    socket.waitConnection()

                } catch (e: TimeoutCancellationException) {
                    false
                }
            }
            println("Awaiting")
            val r: Boolean = result.await()
            println("Awaited")
            if (r) {
                println("Connection established on port $port")
            } else {
                println("Connection failed on port $port")
                throw Exception("Connection failed on port $port")
            }

            while (true) {
                val receivedPacket = socket.getMessage()
                println(receivedPacket)
                val message = JsonService.fromPayload(receivedPacket.payload)
                when (message) {
                    is SimpleMessage -> {
                        val localMessageId = message.localMessageId
                        val totalMessagesLen = message.numberOfPackets
                        messageCollectors.computeIfAbsent(localMessageId) {
                            MessageCollector(
                                localMessageId,
                                totalMessagesLen
                            )
                        }.addMessage(message)
                        if (messageCollectors[localMessageId]!!.isComplete()) {
                            val completeMessage = messageCollectors[localMessageId]!!.getCompleteMessage()
                            notifyListenersMessage(completeMessage)
                            messageCollectors.remove(localMessageId)
                        }
                    }

                    is FileMessage -> {
                        val fileName = message.fileName
                        val totalPackets = message.numberOfPackets
                        fileCollectors.computeIfAbsent(fileName) {
                            FileCollector(
                                fileName,
                                totalPackets
                            )
                        }.addPacket(message)

                        if (fileCollectors[fileName]!!.isComplete()) {
                            val completeFile = fileCollectors[fileName]!!.getCompleteFile()
                            notifyListenersFile(fileName, completeFile)
                            fileCollectors.remove(fileName)
                        }
                    }

                    else -> {
                        throw IllegalStateException("Unknown message type")
                    }
                }
            }
        }

    }

    private fun notifyListenersMessage(message: String) {
        for (listener in messageListeners) {
            listener.onMessageReceive(message)
        }
    }

    private fun notifyListenersFile(fileName: String, fileContent: ByteArray) {
        for (listener in messageListeners) {
            listener.onFileReceive(fileName, fileContent)
        }
    }
}