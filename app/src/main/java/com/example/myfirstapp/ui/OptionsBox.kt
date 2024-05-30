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
import androidx.compose.material.icons.filled.Money
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

@SuppressLint("MutableCollectionMutableState")
@Composable
fun CustomDialog(
    historyItems: MutableList<String>,
    onTextChange: (String) -> Unit,
    onSearchClicked: (MutableList<String>) -> Unit,
    onCloseClicked: () -> Unit,
    setShowDialog: (Boolean) -> Unit,

) {
    if (historyItems.isEmpty()) {
        historyItems.add("")
    }
    //val txtFieldError = remember { mutableStateOf("") }
    val historyItemsState = remember { mutableStateOf(historyItems) }

    Dialog(onDismissRequest = { setShowDialog(false) }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White
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

                    historyItems.forEachIndexed { index, sensorIdText ->
                        Input(historyItems, onSearchClicked, onTextChange, index, sensorIdText,
                            {
                                editedId -> historyItems[index] = editedId
                            },
                            {
                                historyItems.removeAt(index)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (historyItems.size > 4) {
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
                                historyItems.add("")
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
                                historyItems.add("")
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
                                onSearchClicked(historyItemsState.value)
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
    historyItems: List<String>,
    onSearchClicked: (MutableList<String>) -> Unit,
    onTextChange: (String) -> Unit,
    index: Int,
    sensorIdText: String,
    onEdit: (String) -> (Unit),
    onRemove: () -> (Unit)
) {
    //val txtFieldError = remember { mutableStateOf("") }
    val txtField = remember { mutableStateOf(sensorIdText) }
    val historyItemsState = remember { mutableStateOf(historyItems) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    )
    {
        TextField(
            modifier = Modifier
                .weight(1f)
                .border(
                    BorderStroke(
                        width = 2.dp,
                        color = colorResource(id = android.R.color.holo_green_light) /* if (onTextChange.toString().isEmpty()) R.color.holo_green_light else R.color.holo_red_dark) */
                    ),
                    shape = RoundedCornerShape(50)
                ),
            shape = RoundedCornerShape(50),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Money,
                    contentDescription = "",
                    tint = colorResource(android.R.color.holo_green_light),
                    modifier = Modifier
                        .width(20.dp)
                        .height(20.dp)
                )
            },
            placeholder = { Text(text = "Enter sensor id") },
            value = txtField.value,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    onSearchClicked(historyItemsState.value.toMutableList())
                }),
            onValueChange = {
                txtField.value = it.take(10)
                onEdit(it.take(10))
                onTextChange(it)
            }
        )
            if ((index == historyItems.size - 1) and (historyItems.size > 1)) {
                IconButton(
                    onClick = { onRemove() }
                    ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Back"
                    )
                }
            } else {
                IconButton(
                    onClick = { onRemove() },
                    enabled = false) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        tint = Color.Transparent,
                        contentDescription = "Back"
                    )
                }
            }
    }
}