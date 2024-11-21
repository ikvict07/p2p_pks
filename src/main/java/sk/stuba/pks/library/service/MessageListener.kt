package sk.stuba.pks.library.service

interface MessageListener {
    fun onMessageReceive(
        message: String,
        connection: String? = null,
    )

    fun onFileReceive(
        fileName: String,
        fileContent: ByteArray,
        connection: String? = null,
    )
}
