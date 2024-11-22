package sk.stuba.pks.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import sk.stuba.pks.starter.ConnectionsState

@Composable
@Preview
@Suppress("FunctionName")
fun App(connectionsState: ConnectionsState) {
    MaterialTheme {
        val currentScreen = remember { mutableStateOf("MainScreen") }
        val v = currentScreen.value
        when {
            v == "MainScreen" -> {
                MainScreen(connectionsState, state = currentScreen)
            }

            v == "ListReadConnection" -> {
                ListReadConnection(connectionsState.connections.values.toList(), state = currentScreen)
            }

            v == "ListSendConnection" -> {
                ListSendConnection(connectionsState.connections.values.toList(), state = currentScreen)
            }

            v.split("$").first() == "ReadScreen" -> {
                ReadScreen(v.split("$").last(), state = currentScreen)
            }

            v.split("$").first() == "SendScreen" -> {
                val connection = v.split("$").last()
                val connectionPort = connection.split(":").last()
                SendScreen(connectionsState.connections.values.first { it.remotePort == connectionPort.toInt() }, state = currentScreen)
            }
        }
    }
}

@Composable
@Preview
@Suppress("FunctionName")
fun MainScreen(
    connectionsState: ConnectionsState,
    state: MutableState<String>,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(0.80f),
        ) {
            Button(modifier = Modifier.weight(1f), onClick = {
                state.value = "ListSendConnection"
            }) {
                Text("Send", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(modifier = Modifier.weight(0.1f))
            Button(modifier = Modifier.weight(1f), onClick = {
                state.value = "ListReadConnection"
            }) {
                Text("Read", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

fun main() =
    application {
        Window(onCloseRequest = ::exitApplication) {
        }
    }
