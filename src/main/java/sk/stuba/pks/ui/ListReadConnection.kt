package sk.stuba.pks.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import sk.stuba.pks.library.enums.SocketConnectionType
import sk.stuba.pks.library.service.SocketConnection

@Composable
@Preview
@Suppress("FunctionName")
fun ListReadConnection(
    listeners: List<SocketConnection>,
    state: MutableState<String>,
) {
    MaterialTheme {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colors.background),
        ) {
            Button(onClick = { state.value = "MainScreen" }, modifier = Modifier.padding(16.dp).align(Alignment.TopStart)) {
                Text("<-")
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "List of read connections",
                    modifier = Modifier.padding(8.dp),
                    color = MaterialTheme.colors.primary,
                    fontSize = MaterialTheme.typography.h4.fontSize,
                )

                listeners.filter { !it.socket.isClosed.get() }.forEach {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier =
                            Modifier
                                .fillMaxWidth(
                                    0.50f,
                                ).clip(CircleShape)
                                .background(MaterialTheme.colors.secondary)
                                .clickable {
                                    state.value = "ReadScreen$${it.port}"
                                },
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = "Listener on port: ${it.port}",
                            color = MaterialTheme.colors.onSecondary,
                            modifier = Modifier.padding(8.dp),
                            textAlign = TextAlign.Center,
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Preview
@Composable
fun lrcp() {
    ListReadConnection(
        listOf(SocketConnection("localhost", SocketConnectionType.LISTENER)),
        state = mutableStateOf("MainScreen"),
    )
}
