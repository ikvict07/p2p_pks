package sk.stuba.pks.library.service

interface MessageListener {
    fun onMessageReceive(
        message: String,
        connection: String,
    )

    fun onFileReceive(
        fileName: String,
        fileContent: ByteArray,
        connection: String,
    )
}
