package sk.stuba.pks.ui.configuration

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import sk.stuba.pks.library.util.ConnectToDto

@Composable
@Suppress("FunctionName")
@Preview
fun ConnectToScreen(
    result: MutableList<ConnectToDto>,
    close: () -> Unit,
) {
    val listeners = remember { mutableStateListOf<ConnectToDto>() }
    MaterialTheme {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colors.background),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    if (listeners.isEmpty()) "No connection" else "You are connecting to: ",
                    modifier = Modifier.padding(8.dp),
                    color = MaterialTheme.colors.primary,
                    fontSize = MaterialTheme.typography.h4.fontSize,
                )

                listeners.forEach {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier =
                            Modifier
                                .fillMaxWidth(
                                    0.50f,
                                ).clip(CircleShape)
                                .background(MaterialTheme.colors.secondary),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = "${it.ip}:${it.port} on port: ${it.myPort}",
                            color = MaterialTheme.colors.onSecondary,
                            modifier = Modifier.padding(8.dp),
                            textAlign = TextAlign.Center,
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                val inputIp = remember { mutableStateOf("") }
                val inputPort = remember { mutableStateOf("") }
                val inputMyPort = remember { mutableStateOf("") }

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(0.8f),
                ) {
                    TextField(
                        value = inputIp.value,
                        onValueChange = { inputIp.value = it },
                        label = { Text("Remote IP") },
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                        modifier = Modifier.padding(16.dp).weight(1f),
                    )
                    Spacer(modifier = Modifier.weight(0.1f))

                    TextField(
                        value = inputPort.value,
                        onValueChange = { inputPort.value = it },
                        label = { Text("Remote Port") },
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                        modifier = Modifier.padding(16.dp).weight(1f),
                    )
                    Spacer(modifier = Modifier.weight(0.1f))

                    TextField(
                        value = inputMyPort.value,
                        onValueChange = { inputMyPort.value = it },
                        label = { Text("Listen on port") },
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                        modifier = Modifier.padding(16.dp).weight(1f),
                    )
                    Spacer(modifier = Modifier.weight(0.1f))

                    Button(onClick = {
                        listeners.add(ConnectToDto(inputPort.value, inputIp.value, inputMyPort.value))
                        result.add(ConnectToDto(inputPort.value, inputIp.value, inputMyPort.value))
                        inputIp.value = ""
                        inputPort.value = ""
                        inputMyPort.value = ""
                    }) {
                        Text("Add")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(onClick = {
                    close()
                }) {
                    Text("Done")
                }
            }
        }
    }
}
