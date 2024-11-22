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
import androidx.compose.foundation.text.KeyboardActions
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

@Composable
@Suppress("FunctionName")
@Preview
fun ListenerPortsScreen(
    result: MutableList<String>,
    close: () -> Unit,
) {
    val listeners = remember { mutableStateListOf<String>() }
    MaterialTheme {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colors.background),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    if (listeners.isEmpty()) "No listeners" else "You are listening on ports:",
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
                            text = "Listener on port: $it",
                            color = MaterialTheme.colors.onSecondary,
                            modifier = Modifier.padding(8.dp),
                            textAlign = TextAlign.Center,
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                val inputPort = remember { mutableStateOf("") }
                TextField(
                    value = inputPort.value,
                    onValueChange = { inputPort.value = it },
                    placeholder = { Text("Enter port number") },
                    modifier =
                        Modifier
                            .fillMaxWidth(0.75f)
                            .padding(16.dp),
                    keyboardOptions =
                        KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Done,
                        ),
                    keyboardActions =
                        KeyboardActions(
                            onDone =
                                {
                                    listeners.add(inputPort.value)
                                    result.add(inputPort.value)
                                    inputPort.value = ""
                                },
                        ),
                    singleLine = true,
                )

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
