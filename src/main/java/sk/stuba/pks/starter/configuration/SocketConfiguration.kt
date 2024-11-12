package sk.stuba.pks.starter.configuration

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(SocketConfigurationProperties::class)
open class SocketConfiguration
