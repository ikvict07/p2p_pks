package sk.stuba.pks.starter.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "ui")
class UiConfigurationProperties {
    var enabled = true
}
