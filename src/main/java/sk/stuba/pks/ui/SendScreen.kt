package sk.stuba.pks.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import sk.stuba.pks.library.service.SocketConnection

@Composable
@Suppress("FunctionName")
fun SendScreen(
    selectedConnection: SocketConnection,
    state: MutableState<String>,
) {
    MaterialTheme {
        val option = remember { mutableStateOf(Options.MESSAGE) }

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            Button(
                onClick = { state.value = "ListSendConnection" },
                modifier = Modifier.padding(16.dp).align(Alignment.TopStart),
            ) {
                Text("<-")
            }
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(8.dp),
                ) {
                    Text(
                        text = "Choose an option:",
                        style = MaterialTheme.typography.h6,
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Options.entries.forEach { optionItem ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(8.dp),
                        ) {
                            RadioButton(
                                selected = option.value == optionItem,
                                onClick = { option.value = optionItem },
                                modifier = Modifier.padding(end = 8.dp),
                            )
                            Text(text = optionItem.name)
                        }
                    }
                }

                Text(
                    text = "Selected option: ${option.value}",
                    style = MaterialTheme.typography.body1,
                )

                when (option.value) {
                    Options.MESSAGE, Options.CORRUPTED -> {
                        Text("Enter your message data:")
                        val inputText = remember { mutableStateOf("") }
                        TextField(
                            value = inputText.value,
                            onValueChange = { inputText.value = it },
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .border(1.dp, Color.Gray),
                            keyboardOptions =
                                KeyboardOptions.Default.copy(
                                    imeAction = ImeAction.Done,
                                ),
                        )

                        Text("Enter max packet size:")
                        val size = remember { mutableStateOf("") }
                        TextField(
                            value = size.value,
                            onValueChange = { size.value = it },
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .border(1.dp, Color.Gray),
                            keyboardOptions =
                                KeyboardOptions.Default.copy(
                                    imeAction = ImeAction.Done,
                                ),
                        )
                        Button(
                            onClick = {
                                if (option.value == Options.MESSAGE) {
                                    selectedConnection.sendMessage(inputText.value, size.value.toLong())
                                } else {
                                    selectedConnection.sendCorruptedMessage(inputText.value, size.value.toLong())
                                }
                            },
                        ) {
                            Text("Send")
                        }
                    }

                    Options.FILE -> {
                        val path = remember { mutableStateOf("") }
                        Text("Enter file path:")
                        TextField(
                            value = path.value,
                            onValueChange = { path.value = it },
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .border(1.dp, Color.Gray),
                            keyboardOptions =
                                KeyboardOptions.Default.copy(
                                    imeAction = ImeAction.Done,
                                ),
                        )
                        Text("Enter max packet size:")
                        val size = remember { mutableStateOf("") }
                        TextField(
                            value = size.value,
                            onValueChange = { size.value = it },
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .border(1.dp, Color.Gray),
                            keyboardOptions =
                                KeyboardOptions.Default.copy(
                                    imeAction = ImeAction.Done,
                                ),
                        )
                        Button(
                            onClick = {
                                println("Sending file: ${path.value}")
                                selectedConnection.sendFile(path.value, size.value.toLong())
                            },
                        ) {
                            Text("Send")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        selectedConnection.closeConnection()
                        state.value = "ListSendConnection"
                    },
                ) {
                    Text("Disconnect")
                }
            }
        }
    }
}

enum class Options {
    MESSAGE,
    FILE,
    CORRUPTED,
}
