package sk.stuba.pks.client.controller

import sk.stuba.pks.library.ConnectTo
import sk.stuba.pks.library.MessageListener


@ConnectTo(address = "localhost", port = "7081", myPort = "7082")
class ConnectorController: MessageListener {
    override fun onMessageReceive(message: String) {
        TODO("Not yet implemented")
    }

    override fun onFileReceive(fileName: String, fileContent: ByteArray) {
        TODO("Not yet implemented")
    }
}
