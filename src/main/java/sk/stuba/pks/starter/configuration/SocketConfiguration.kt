package sk.stuba.pks.starter.configuration

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(value = [SocketConfigurationProperties::class, UiConfigurationProperties::class, GeneralProperties::class])
open class SocketConfiguration
