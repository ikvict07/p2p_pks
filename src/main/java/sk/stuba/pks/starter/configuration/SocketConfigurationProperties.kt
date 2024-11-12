package sk.stuba.pks.starter.configuration

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Positive
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@Validated
@ConfigurationProperties(prefix = "socket")
data class SocketConfigurationProperties(
    @field:Min(1) @field:Max(800)
    val maxPayloadSize: Long = 800,
    @field:Positive
    val attemptsToReconnect: Long = 3,
    @field:Positive
    val connectionTimeoutMs: Long = 30_000,
    @field:Positive
    val messageResendingFrequencyMs: Long = 50,
    @field:Positive
    val messageResendingConfirmationTimeMs: Long = 500,
    @field:Positive
    val keepAliveFrequencyMs: Long = 5000,
    @field:Positive
    val retryToConnectEveryMs: Long = 3_000,
)
