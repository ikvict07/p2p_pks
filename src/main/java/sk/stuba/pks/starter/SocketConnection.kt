package sk.stuba.pks.starter

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import sk.stuba.pks.library.CustomSocket
import sk.stuba.pks.library.FileCollector
import sk.stuba.pks.library.InjectMassageListeners
import sk.stuba.pks.library.MessageCollector
import sk.stuba.pks.library.MessageListener
import sk.stuba.pks.old.model.FileMessage
import sk.stuba.pks.old.model.SimpleMessage
import sk.stuba.pks.old.service.PacketReceiveListener
import sk.stuba.pks.old.service.mapping.JsonService

class SocketConnection(
    val port: String,
) {
    @InjectMassageListeners
    private lateinit var messageListeners: List<MessageListener>

    private lateinit var remoteIp: String

    val socket = CustomSocket(port)
    private val messageCollectors = mutableMapOf<Int, MessageCollector>()
    private val fileCollectors = mutableMapOf<String, FileCollector>()
    private var isConnectionEstablished = false

    fun getRemoteIp(): String =
        if (isConnectionEstablished) {
            remoteIp
        } else {
            ""
        }

    fun isConnectionEstablished(): Boolean = isConnectionEstablished

    private fun startSending() {
        CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                if (!isConnectionEstablished) {
                    delay(1000)
                } else {
                    break
                }
            }
            socket.packetListeners.addAll(
                listOf(
                    PacketReceiveListener { packet -> {} },
                    PacketReceiveListener { packet ->
                        run {
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
                    },
                ),
            )
            socket.startSending()
        }
    }

    fun initListener() {
        startListening()
        startSending()
    }

    fun initConnector(
        serverAddress: String,
        serverPort: Int,
    ) {
        connect(serverAddress, serverPort)
        startSending()
    }

    fun sendMessage(message: String) {
        if (!isConnectionEstablished) {
            throw IllegalStateException("Connection not established")
        }

        CoroutineScope(Dispatchers.IO).launch {
            socket.sendMessage(message)
        }
    }

    fun sendFile(filePath: String) {
        if (!isConnectionEstablished) {
            throw IllegalStateException("Connection not established")
        }

        CoroutineScope(Dispatchers.IO).launch {
            socket.sendFile(filePath)
        }
    }

    fun connect(
        serverAddress: String,
        serverPort: Int,
    ) {
        println("Trying to connect")
        CoroutineScope(Dispatchers.IO).launch {
            val result =
                async {
                    try {
                        socket.connect(serverAddress, serverPort)
                    } catch (e: TimeoutCancellationException) {
                        false
                    }
                }
            println("Awaiting")
            val r: Boolean = result.await()
            println("Awaited")
            if (r) {
                println("Connection established on port $port")
                remoteIp = serverAddress
            } else {
                println("Connection failed on port $port")
                throw Exception("Connection failed on port $port")
            }
            isConnectionEstablished = true
        }
    }

    fun startListening() {
        println("Listening on port HHH $port")
        CoroutineScope(Dispatchers.IO).launch {
            val result =
                async {
                    try {
                        println("Trying")
                        socket.waitConnection()
                    } catch (e: TimeoutCancellationException) {
                        ""
                    }
                }
            println("Awaiting")
            val r: String = result.await()
            println("Port $port result $r")
            println("Awaited")
            if (r.isNotEmpty()) {
                println("Connection established on port $port")
                remoteIp = r
            } else {
                println("Connection failed on port $port")
                throw Exception("Connection failed on port $port")
            }
            isConnectionEstablished = true
        }
    }

    fun closeConnection() {
        TODO("Not yet implemented")
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
