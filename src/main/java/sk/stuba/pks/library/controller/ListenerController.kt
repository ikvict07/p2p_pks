package sk.stuba.pks.library.controller

import org.reflections.Reflections.log
import org.springframework.stereotype.Component
import sk.stuba.pks.library.service.MessageListener
import sk.stuba.pks.starter.configuration.GeneralProperties
import sk.stuba.pks.starter.configuration.UiConfigurationProperties
import sk.stuba.pks.ui.Printer
import sk.stuba.pks.ui.sendMessageGlobally
import java.nio.file.Files
import java.nio.file.Paths

@Component
class ListenerController(
    private val generalProperties: GeneralProperties,
    private val uiConfigurationProperties: UiConfigurationProperties
) : MessageListener {
    override fun onMessageReceive(
        message: String,
        connection: String,
    ) {
        log.info("Received message: $message")
        if (uiConfigurationProperties.enabled) {

            sendMessageGlobally(connection, message)
        } else {
            Printer.printMessage(message, connection)
        }
    }

    override fun onFileReceive(
        fileName: String,
        fileContent: ByteArray,
        connection: String,
    ) {
        val directory = Paths.get(generalProperties.fileSaveLocation)
        if (!Files.exists(directory)) {
            Files.createDirectories(directory)
        }

        val path = directory.resolve(fileName)
        if (!Files.exists(path)) {
            Files.createFile(path)
        }
        Files.write(path, fileContent)
        log.info("Received file: $fileName")
        if (uiConfigurationProperties.enabled) {
            sendMessageGlobally(connection, "File $fileName received")
        } else {
            Printer.printFile(fileName, connection)
        }
    }
}
