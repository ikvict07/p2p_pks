package sk.stuba.pks.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

object MessageManager {
    private val _messages = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val messages = _messages.asStateFlow()
    private val messageChannel = Channel<Pair<String, String>>(Channel.UNLIMITED)

    init {
        CoroutineScope(Dispatchers.Default).launch {
            messageChannel.receiveAsFlow().collect { (connection, message) ->
                _messages.value =
                    _messages.value.toMutableMap().apply {
                        this[connection] = (this[connection] ?: emptyList()) + message
                    }
            }
        }
    }

    fun sendMessage(
        connection: String,
        message: String,
    ) {
        messageChannel.trySend(connection to message)
    }
}

@Composable
@Preview
@Suppress("FunctionName")
fun ReadScreen(
    selectedConnection: String,
    state: MutableState<String>,
) {
    val messages by MessageManager.messages.collectAsState()

    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Button(
                onClick = { state.value = "ListReadConnection" },
                modifier = Modifier.padding(16.dp).align(Alignment.TopStart),
            ) {
                Text("<-")
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    "Messages for $selectedConnection",
                    style = MaterialTheme.typography.h4,
                    color = MaterialTheme.colors.primary,
                    modifier = Modifier.padding(8.dp),
                )
                messages[selectedConnection]?.forEach {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier =
                            Modifier
                                .fillMaxWidth(
                                    0.50f,
                                ).background(MaterialTheme.colors.secondary)
                                .padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = it,
                            color = MaterialTheme.colors.onSecondary,
                            modifier = Modifier.padding(8.dp).weight(1f),
                            textAlign = TextAlign.Center,
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

fun sendMessageGlobally(
    connection: String,
    message: String,
) {
    MessageManager.sendMessage(connection, message)
}
