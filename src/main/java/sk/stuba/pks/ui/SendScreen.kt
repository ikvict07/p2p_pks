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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import dev.nesk.akkurate.ValidationResult
import dev.nesk.akkurate.Validator
import dev.nesk.akkurate.constraints.builders.isMatching
import dev.nesk.akkurate.constraints.builders.isNotEmpty
import dev.nesk.akkurate.constraints.constrain
import sk.stuba.pks.library.service.SocketConnection
import sk.stuba.pks.library.validators.Answer
import sk.stuba.pks.library.validators.validation.accessors.answer
import java.nio.file.Files

@Composable
@Suppress("FunctionName")
fun SendScreen(
    selectedConnection: SocketConnection,
    state: MutableState<String>,
) {
    MaterialTheme {
        val option = remember { mutableStateOf(Options.MESSAGE) }

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
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

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "Selected option: ${option.value}",
                        style = MaterialTheme.typography.body1,
                    )
                }

                when (option.value) {
                    Options.MESSAGE, Options.CORRUPTED -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(8.dp),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Text("Enter your message data:")
                        }
                        val inputText = remember { mutableStateOf("") }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(8.dp),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            TextField(
                                placeholder = { Text("Enter your message data") },
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
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(8.dp),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Text("Enter max packet size:")
                        }
                        val size = remember { mutableStateOf("") }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(8.dp),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            TextField(
                                placeholder = { Text("Enter max packet size (1-800)") },
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
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(8.dp),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Button(
                                onClick = {
                                    val validator =
                                        Validator<Answer> {
                                            answer.isNotEmpty()
                                        }
                                    val res = validator(Answer(inputText.value))
                                    if (res is ValidationResult.Failure) {
                                        return@Button
                                    }

                                    val validator2 =
                                        Validator<Answer> {
                                            answer.isNotEmpty()
                                            answer.isMatching("^[1-9][0-9]{0,2}$".toRegex())
                                            answer.constrain {
                                                try {
                                                    it.toInt() in 1..800
                                                } catch (e: Exception) {
                                                    false
                                                }
                                            }
                                        }
                                    val sizeRes = validator2(Answer(size.value))
                                    if (sizeRes is ValidationResult.Failure) {
                                        return@Button
                                    }

                                    if (option.value == Options.MESSAGE) {
                                        selectedConnection.sendMessage(inputText.value, size.value.toLong())
                                    } else {
                                        selectedConnection.sendCorruptedMessage(inputText.value, size.value.toLong())
                                    }
                                    inputText.value = ""
                                    size.value = ""
                                },
                            ) {
                                Text("Send")
                            }
                        }
                    }

                    Options.FILE -> {
                        val path = remember { mutableStateOf("") }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(8.dp),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Text("Enter file path:")
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(8.dp),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            TextField(
                                placeholder = { Text("Enter file path") },
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
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(8.dp),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Text("Enter max packet size:")
                        }
                        val size = remember { mutableStateOf("") }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(8.dp),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            TextField(
                                placeholder = { Text("Enter max packet size (1-800)") },
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
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(8.dp),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Button(
                                onClick = {
                                    val validator =
                                        Validator<Answer> {
                                            answer.isNotEmpty()
                                            answer.constrain {
                                                try {
                                                    Files.exists(
                                                        java.nio.file.Path
                                                            .of(it),
                                                    )
                                                } catch (e: Exception) {
                                                    false
                                                }
                                            }
                                        }
                                    val res = validator(Answer(path.value))
                                    if (res is ValidationResult.Failure) {
                                        return@Button
                                    }

                                    val validator2 =
                                        Validator<Answer> {
                                            answer.isNotEmpty()
                                            answer.isMatching("^[1-9][0-9]{0,2}$".toRegex())
                                            answer.constrain {
                                                try {
                                                    it.toInt() in 1..800
                                                } catch (e: Exception) {
                                                    false
                                                }
                                            }
                                        }
                                    val sizeRes = validator2(Answer(size.value))
                                    if (sizeRes is ValidationResult.Failure) {
                                        return@Button
                                    }
                                    selectedConnection.sendFile(path.value, size.value.toLong())
                                    path.value = ""
                                    size.value = ""
                                },
                            ) {
                                Text("Send")
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
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

            Button(
                onClick = { state.value = "ListSendConnection" },
                modifier = Modifier.padding(16.dp).align(Alignment.TopStart),
            ) {
                Text("<-")
            }
        }
    }
}

enum class Options {
    MESSAGE,
    FILE,
    CORRUPTED,
}
