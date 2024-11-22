package sk.stuba.pks.library.service

import jakarta.annotation.PostConstruct
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.reflections.Reflections.log
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import sk.stuba.pks.library.enums.SocketConnectionType
import sk.stuba.pks.starter.configuration.SocketConfigurationProperties
import kotlin.properties.Delegates

class SocketConnection(
    val port: String,
    val type: SocketConnectionType,
) {
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    @Lazy
    private lateinit var messageListeners: List<MessageListener>

    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private lateinit var socketConfigurationProperties: SocketConfigurationProperties
    private var remoteIp: String? = null
    lateinit var socket: CustomSocket
    var remotePort by Delegates.notNull<Int>()
    private var isConnectionEstablished = false
    private lateinit var messageReceiveListener: PacketReceiveListener

    @Suppress("unused")
    constructor(port: Int, remoteIP: String, remotePort: Int, type: SocketConnectionType) : this(
        port.toString(),
        type,
    ) {
        remoteIp = remoteIP
        this.remotePort = remotePort
    }

    @PostConstruct
    fun init() {
        messageReceiveListener = PacketReceiveListenerImpl(messageListeners)
        socket = CustomSocket(port, socketConfigurationProperties)
    }

    fun initListener() {
        CoroutineScope(Dispatchers.IO).launch {
            startListening()
            startSending()
        }
    }

    fun initConnector(
        serverAddress: String,
        serverPort: Int,
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            connect(serverAddress, serverPort)
            startSending()
        }
    }

    fun getRemoteIp(): String {
        runBlocking {
            withTimeout(socketConfigurationProperties.connectionTimeoutMs) {
                while (remoteIp == null) {
                    Thread.sleep(1000)
                }
            }
        }
        return remoteIp!!
    }

    private suspend fun startSending() {
        coroutineScope {
            withTimeout(socketConfigurationProperties.connectionTimeoutMs) {
                while (true) {
                    if (!isConnectionEstablished) {
                        delay(1000)
                    } else {
                        break
                    }
                }
            }
            socket.packetListeners.addAll(listOf(messageReceiveListener))
            socket.startSending()
        }
    }

    fun sendMessage(
        message: String,
        maxPacketSize: Long,
    ) {
        if (!isConnectionEstablished) {
            throw IllegalStateException("Connection not established")
        }
        if (socket.isClosed.get()) {
            log.error("This connection is closed")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            socket.sendMessage(message, maxPacketSize)
        }
    }

    fun sendFile(
        filePath: String,
        maxPacketSize: Long,
    ) {
        if (!isConnectionEstablished) {
            throw IllegalStateException("Connection not established")
        }
        if (socket.isClosed.get()) {
            log.error("This connection is closed")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            socket.sendFile(filePath, maxPacketSize)
        }
    }

    private suspend fun connect(
        serverAddress: String,
        serverPort: Int,
    ) {
        coroutineScope {
            val result =
                async {
                    try {
                        socket.connect(serverAddress, serverPort)
                    } catch (e: TimeoutCancellationException) {
                        false
                    }
                }
            val r: Boolean = result.await()
            if (r) {
                log.info("Connection established on port $port")
                remoteIp = serverAddress
                remotePort = serverPort
            } else {
                log.error("Connection failed on port $port")
                throw Exception("Connection failed on port $port")
            }
            isConnectionEstablished = true
        }
    }

    private suspend fun startListening() {
        coroutineScope {
            val result =
                async {
                    try {
                        socket.waitConnection()
                    } catch (e: TimeoutCancellationException) {
                        throw RuntimeException("Timeout")
                    }
                }
            val r: String = result.await()
            if (r.isNotEmpty()) {
                log.info("Connection established on port $port")
                remoteIp = r
                remotePort = socket.serverPort
            } else {
                log.error("Connection failed on port $port")
                throw Exception("Connection failed on port $port")
            }
            isConnectionEstablished = true
        }
    }

    fun sendCorruptedMessage(
        message: String,
        maxPacketSize: Long,
    ) {
        if (!isConnectionEstablished) {
            throw IllegalStateException("Connection not established")
        }
        if (socket.isClosed.get()) {
            log.error("This connection is closed")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            socket.sendCorruptedMessage(message, maxPacketSize)
        }
    }

    fun closeConnection() {
        socket.sendFin()
    }
}
