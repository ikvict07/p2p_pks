package sk.stuba.pks.starter

interface MessageListener {
    fun onMessageReceive(message: String)
    fun onFileReceive(fileName: String, fileContent: ByteArray)
}
