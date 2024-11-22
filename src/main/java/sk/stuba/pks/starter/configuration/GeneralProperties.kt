package sk.stuba.pks.starter.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "general")
class GeneralProperties {
    var fileSaveLocation: String = "src/main/resources"
}