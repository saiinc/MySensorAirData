package com.example.myfirstapp.ui


import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
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
import com.example.myfirstapp.data.SettingsSensor

@SuppressLint("MutableCollectionMutableState")
@Composable
fun CustomDialog(
    settingsItems: MutableList<SettingsSensor>,

    onTextChange: (String) -> Unit,
    onSettingsChange: (List<SettingsSensor>) -> List<SettingsSensor>,
    onDoneClicked: (List<SettingsSensor>) -> Unit,
    onCloseClicked: () -> Unit,
    setShowDialog: (Boolean) -> Unit,

    ) {
    if (settingsItems.isEmpty()) {
        settingsItems.add(SettingsSensor(id = "", description = ""))
    }

    //val txtFieldError = remember { mutableStateOf("") }

    Dialog(
        properties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = { setShowDialog(false) }
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
            //color = MaterialTheme.colorScheme.background
            //color = Color.White
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            //modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            text = "Set sensor ID",
                            style = TextStyle(
                                fontSize = 24.sp,
                                fontFamily = FontFamily.Default,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Icon(
                            imageVector = Icons.Filled.Cancel,
                            contentDescription = "",
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

                    settingsItems.forEachIndexed { index, sensorIdObject ->
                        Input(settingsItems, onDoneClicked, onTextChange, onSettingsChange, index, sensorIdObject,
                            {
                                editedId -> settingsItems[index].id = editedId
                            },
                            {
                                editedDescription -> settingsItems[index].description = editedDescription
                            },
                            {
                                settingsItems.removeAt(index)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (settingsItems.size > 5) {
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
                    }
                    else {
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
                                settingsItems.add(SettingsSensor(id = "", description = ""))
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
                        Button(
                            onClick = {
                                /*if (onTextChange.toString().isEmpty()) {
                                    onTextChange("Field can not be empty")
                                    txtFieldError.value = "Field can not be empty"
                                    return@Button
                                }*/
                                onDoneClicked(onSettingsChange(settingsItems.distinct()))
                                //setShowDialog(false)
                            },
                            shape = RoundedCornerShape(50.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text(text = "Done")
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun Input(
    settingsItems: MutableList<SettingsSensor>,
    onDoneClicked: (List<SettingsSensor>) -> Unit,
    onTextChange: (String) -> Unit,
    onSettingsChange: (List<SettingsSensor>) -> List<SettingsSensor>,
    index: Int,
    sensorIdObject: SettingsSensor,
    onEditId: (String) -> (Unit),
    onEditDescription: (String) -> (Unit),
    onRemove: () -> (Unit)
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
            /*leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Money,
                    contentDescription = "",
                    tint = colorResource(android.R.color.holo_green_light),
                    modifier = Modifier
                        .width(20.dp)
                        .height(20.dp)
                )
            },*/
            placeholder = { Text(text = "Enter id") },
            maxLines = 1,
            value = txtFieldId.value,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            /*keyboardActions = KeyboardActions(
                onDone = {
                    onDoneClicked(onSettingsChange(settingsItems.distinct()))
                }),*/
            onValueChange = {
                value -> txtFieldId.value = (value.filter { it.isDigit() }).take(7)
                //txtField.value = it.take(10)
                onEditId(value.take(7))
                onTextChange(value)
            }
        )
        TextField(
            modifier = Modifier
                .weight(2f)
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
            placeholder = {Text(text = "Description (optional)")},
            maxLines = 1,
            value = txtFieldDescription.value,
            onValueChange = { value ->
                txtFieldDescription.value = value.take(23)
                onEditDescription(value.take(23))
                onTextChange(value)
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            )
        )
            if ((index == settingsItems.size - 1) and (settingsItems.size > 1)) {
                IconButton(
                    modifier = Modifier.padding(vertical = 2.dp),
                    onClick = { onRemove() }
                    ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Delete"
                    )
                }
            } else {
                IconButton(
                    modifier = Modifier.padding(vertical = 2.dp),
                    onClick = { onRemove() },
                    enabled = false) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        tint = Color.Transparent,
                        contentDescription = "Delete"
                    )
                }
            }
    }
}