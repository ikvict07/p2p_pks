package sk.stuba.pks.library

interface MessageListener {
    fun onMessageReceive(message: String)
    fun onFileReceive(fileName: String, fileContent: ByteArray)
}
