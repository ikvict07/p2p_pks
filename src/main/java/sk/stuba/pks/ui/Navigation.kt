package sk.stuba.pks.ui

import sk.stuba.pks.library.service.SocketConnection
import sk.stuba.pks.starter.ConnectionsState

object MainScreenDto

data class ReadScreenDto(
    val id: String,
    val connections: ConnectionsState,
)

data class ListReadConnectionDto(
    val connections: List<SocketConnection>,
)
