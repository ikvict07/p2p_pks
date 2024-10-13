package sk.stuba.pks.client.controller

import sk.stuba.pks.starter.ListenPort
import sk.stuba.pks.starter.MessageListener

@ListenPort(port= "7081")
class MainController: MessageListener {
    override fun onMessageReceive(message: String) {
        println("Received message: $message")
    }

    override fun onFileReceive(fileName: String, fileContent: ByteArray) {
        TODO("Not yet implemented")
    }
}