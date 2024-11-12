package sk.stuba.pks.starter

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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import sk.stuba.pks.library.CustomSocket
import sk.stuba.pks.library.MessageListener
import sk.stuba.pks.old.service.PacketReceiveListener
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

    private suspend fun connect(
        serverAddress: String,
        serverPort: Int,
    ) {
        println("Trying to connect")
        coroutineScope {
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
                remotePort = serverPort
            } else {
                println("Connection failed on port $port")
                throw Exception("Connection failed on port $port")
            }
            println("WE ENDED CONNECTING, PORT $port, REMOTE IP $remoteIp, REMOTE PORT $remotePort")
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
            println("Port $port result $r")
            println("Awaited")
            if (r.isNotEmpty()) {
                println("Connection established on port $port")
                remoteIp = r
                remotePort = socket.serverPort
            } else {
                println("Connection failed on port $port")
                throw Exception("Connection failed on port $port")
            }
            println("WE ENDED STARTING LISTENING, PORT $port, REMOTE IP $remoteIp, REMOTE PORT $remotePort")
            isConnectionEstablished = true
        }
    }

    fun closeConnection() {
        TODO("Not yet implemented")
    }
}
