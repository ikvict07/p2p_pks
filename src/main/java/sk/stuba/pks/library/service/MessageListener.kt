package sk.stuba.pks.library.service

interface MessageListener {
    fun onMessageReceive(message: String)

    fun onFileReceive(
        fileName: String,
        fileContent: ByteArray,
    )
}
