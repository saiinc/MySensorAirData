/*
 * Copyright © Anton Sorokin 2025. All rights reserved
 */

package com.saionji.mysensor.ui



import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.saionji.mysensor.C
import com.saionji.mysensor.R
import com.saionji.mysensor.data.SettingsApp
import com.saionji.mysensor.data.SettingsSensor

@SuppressLint("MutableCollectionMutableState")
@Composable
fun CustomDialog(
    sensorsOptions: State<List<SettingsSensor>>,
    settingsApp: SettingsApp,
    onTextChange: (String) -> Unit,
    onAppSettingsChange: (SettingsApp) -> Unit,
    onAddClicked: () -> Unit,
    onRemoveClicked: (SettingsSensor) -> Unit,
    onEditSensorId: (Int, String) -> Unit,
    onEditSensorDescription: (Int, String) -> Unit,
    onDoneClicked: (State<List<SettingsSensor>>, SettingsApp) -> Unit,
    onCloseClicked: () -> Unit,
    setShowDialog: (Boolean) -> Unit,

    ) {
    if (sensorsOptions.value.isEmpty()) {
        onAddClicked()
    }
    //val txtFieldError = remember { mutableStateOf("") }

    Dialog(
        properties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = { setShowDialog(false) }
    ) {
        Box(
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.surface, // Фон диалога
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(
                        Color.White.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            textAlign = TextAlign.Center,
                            text = stringResource(id = R.string.settings_header),
                            style = TextStyle(
                                fontSize = 24.sp,
                                fontFamily = FontFamily.Default,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Icon(
                            imageVector = Icons.Filled.Cancel,
                            contentDescription = "Cancel",
                            tint = colorResource(android.R.color.darker_gray),
                            modifier = Modifier
                                .width(30.dp)
                                .height(30.dp)
                                .clickable {
                                    //setShowDialog(false)
                                    onCloseClicked()
                                }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 15.dp),
                                textAlign = TextAlign.Left,
                                style = TextStyle(
                                    fontSize = 18.sp,
                                    fontFamily = FontFamily.Default
                                ),
                                text = stringResource(id = R.string.settings_switch)
                            )
                            Switch(
                                checked = settingsApp.shareId,
                                onCheckedChange = { value ->
                                    onAppSettingsChange(SettingsApp(value))
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        sensorsOptions.value.forEachIndexed { index, sensorIdObject ->
                            Input(
                                sensorsOptions = sensorsOptions,
                                onTextChange = onTextChange,
                                sensorIdObject = sensorIdObject,
                                onEditId = { editedId ->
                                    onEditSensorId(index, editedId)
                                },
                                onEditDescription = { editedDescription ->
                                    onEditSensorDescription(index, editedDescription)
                                },
                                onRemoveClicked = {
                                    onRemoveClicked(sensorIdObject)
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if ((sensorsOptions.value.size > C.DASHBOARD_SENSOR_LIMIT) or
                        ((sensorsOptions.value.size == 1) && ((sensorsOptions.value[0].id == "") && (sensorsOptions.value[0].description == "")))) {
                        IconButton(
                            modifier = Modifier
                                .border(
                                    BorderStroke(
                                        width = 2.dp,
                                        color = colorResource(android.R.color.holo_green_light)
                                    ),
                                    shape = RoundedCornerShape(15)
                                ),
                            onClick = {
                            },
                            enabled = false
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Add"
                            )
                        }
                    } else {
                        IconButton(
                            modifier = Modifier
                                .border(
                                    BorderStroke(
                                        width = 2.dp,
                                        color = colorResource(android.R.color.holo_green_light)
                                    ),
                                    shape = RoundedCornerShape(15)
                                ),
                            onClick = {
                                onAddClicked()
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Add"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Box(modifier = Modifier.padding(40.dp, 0.dp, 40.dp, 0.dp)) {
                        if (!((sensorsOptions.value.size == 1) && ((sensorsOptions.value[0].id == "") && (sensorsOptions.value[0].description == "")))) {
                            Button(
                                onClick = {
                                    /*if (onTextChange.toString().isEmpty()) {
                                onTextChange("Field can not be empty")
                                txtFieldError.value = "Field can not be empty"
                                return@Button
                            }*/
                                    onDoneClicked(
                                        sensorsOptions,
                                        settingsApp
                                    )
                                    //setShowDialog(false)
                                },
                                shape = RoundedCornerShape(50.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            ) {
                                Text(text = stringResource(id = R.string.settings_button))
                            }
                        } else {
                            Button(
                                enabled = false,
                                onClick = {},
                                shape = RoundedCornerShape(50.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            ) {
                                Text(text = stringResource(id = R.string.settings_button))
                            }
                        }
                    }
                }
            }
        }
    }
}



@Composable
fun Input(
    sensorsOptions: State<List<SettingsSensor>>,
    onTextChange: (String) -> Unit,
    sensorIdObject: SettingsSensor,
    onEditId: (String) -> (Unit),
    onEditDescription: (String) -> (Unit),
    onRemoveClicked: () -> (Unit)
) {
    //val txtFieldError = remember { mutableStateOf("") }
    val txtFieldId = remember {
        mutableStateOf(sensorIdObject.id)
    }
    val txtFieldDescription = remember {
        mutableStateOf(sensorIdObject.description)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    )
    {
        TextField(
            modifier = Modifier
                .weight(1f)
                .padding(end = 2.dp)
                .border(
                    BorderStroke(
                        width = 2.dp,
                        color = colorResource(id = android.R.color.holo_green_light) /* if (onTextChange.toString().isEmpty()) R.color.holo_green_light else R.color.holo_red_dark) */
                    ),
                    shape = RoundedCornerShape(25)
                ),
            shape = RoundedCornerShape(25),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            placeholder = { Text(text = stringResource(id = R.string.settings_sensor_id)) },
            maxLines = 2,
            value = sensorIdObject.id,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            onValueChange = {
                value -> txtFieldId.value = (value.filter { it.isDigit() }).take(8)
                onEditId(value.filter { it.isDigit() }.take(8))
                onTextChange(value.filter { it.isDigit() })
            }
        )
        TextField(
            modifier = Modifier
                .weight(2.3f)
                .padding(start = 2.dp)
                .border(
                    BorderStroke(
                        width = 2.dp,
                        color = colorResource(id = android.R.color.holo_green_light) /* if (onTextChange.toString().isEmpty()) R.color.holo_green_light else R.color.holo_red_dark) */
                    ),
                    shape = RoundedCornerShape(25)
                ),
            shape = RoundedCornerShape(25),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            placeholder = {Text(text = stringResource(id = R.string.settings_sensor_description))},
            maxLines = 3,
            value = sensorIdObject.description,
            onValueChange = { value ->
                txtFieldDescription.value = value.take(23)
                onEditDescription(value.take(23))
                onTextChange(value)
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            )
        )
        if ((sensorsOptions.value.size == 1)) {
            IconButton(
                modifier = Modifier
                    .padding(vertical = 2.dp)
                    .size(36.dp),
                onClick = { onRemoveClicked() },
                enabled = false) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    //tint = Color.Transparent,
                    contentDescription = "Delete"
                )
            }
        } else {
            IconButton(
                modifier = Modifier
                    .padding(vertical = 2.dp)
                    .size(36.dp),
                onClick = { onRemoveClicked() },
                enabled = true) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Delete"
                )
            }
        }
    }
}