package sk.stuba.pks.starter.configuration

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Positive
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@Validated
@ConfigurationProperties(prefix = "socket")
class SocketConfigurationProperties {
    @field:Min(1)
    @field:Max(800)
    var maxPayloadSize: Long = 800

    @field:Positive
    var attemptsToReconnect: Long = 3

    @field:Positive
    var connectionTimeoutMs: Long = 30000

    @field:Positive
    var messageResendingFrequencyMs: Long = 50

    @field:Positive
    var messageResendingConfirmationTimeMs: Long = 500

    @field:Positive
    var keepAliveFrequencyMs: Long = 5000

    @field:Positive
    var retryToConnectEveryMs: Long = 3000
}
