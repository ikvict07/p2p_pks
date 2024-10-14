package sk.stuba.pks.library

import org.springframework.stereotype.Component

@Component
annotation class ConnectTo(
    val address: String = "localhost",
    val port: String,
    val myPort: String,
)
