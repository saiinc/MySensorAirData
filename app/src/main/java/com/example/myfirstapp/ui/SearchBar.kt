package com.example.myfirstapp.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myfirstapp.R


@Composable
fun MainAppBar(
    searchWidgetState: OptionsBoxState,
    historyItems: MutableList<String>,
    onTextChange: (String) -> Unit,
    onCloseClicked: () -> Unit,
    onSearchClicked: (List<String>) -> Unit,
    onRefreshClicked: (List<String>) -> Unit,
    onSearchTriggered: () -> Unit
) {
    when (searchWidgetState) {
        OptionsBoxState.CLOSED -> {
            ClosedAppBar (
                onSearchClicked = onSearchTriggered,
                onRefreshClicked = onRefreshClicked,
                historyItems = historyItems
            )
        }
        OptionsBoxState.OPENED -> {
            /*OpenedAppBar(
                text = searchTextState,
                onTextChange = onTextChange,
                onCloseClicked = onCloseClicked,
                onSearchClicked = onSearchClicked,
            )*/
            ClosedAppBar (
                onSearchClicked = onSearchTriggered,
                onRefreshClicked = onRefreshClicked,
                historyItems = historyItems
            )
            CustomDialog(
                historyItems = historyItems,
                onTextChange = onTextChange,
                onSearchClicked = onSearchClicked,
                onCloseClicked = onCloseClicked,
                /*setShowDialog = {
                showDialog.value = it
            }*/
            ) {
                Log.i("HomePage","HomePage : $it")
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClosedAppBar(
    onSearchClicked: () -> Unit,
    onRefreshClicked: (List<String>) -> Unit,
    historyItems: List<String>
) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(id = R.string.app_name)
            )
        },
        actions = {
            IconButton(
                onClick = {
                    onRefreshClicked(historyItems)
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "RefreshIcon",
                    tint = Color.Black
                )
            }
            IconButton(
                onClick = {
                    onSearchClicked()
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "SettingsIcon",
                    tint = Color.Black
                )
                
            }
        }
    )
}

@Composable
fun OpenedAppBar(
    text: String,
    onTextChange: (String) -> Unit,
    onCloseClicked: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        color = MaterialTheme.colorScheme.primary
    ) {
        TextField(
            modifier = Modifier
                .fillMaxWidth(),
            value = text,
            onValueChange = {
                onTextChange(it)
            },
            placeholder = {
                Text(
                    modifier = Modifier
                        .alpha(DefaultAlpha),
                    text = "Enter Sensor Id...",
                    color = Color.Black
                )
            },
            textStyle = TextStyle(
                fontSize = MaterialTheme.typography.labelLarge.fontSize
            ),
            singleLine = true,
            leadingIcon = {
                IconButton(
                    modifier = Modifier
                        .alpha(DefaultAlpha)
                        .background(color = Color.LightGray, shape = CircleShape),
                    onClick = { })//onSearchClicked(text) })
                {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = "Search Icon",
                        tint = Color.Black
                    )
                }
            },
            trailingIcon = {
                IconButton(
                    onClick = {
                        if (text.isNotEmpty()) {
                            onTextChange("")
                        } else {
                            onCloseClicked()
                        }
                    },
                    content = {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Icon",
                            tint = Color.Black
                        )
                    }
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    //onSearchClicked(text)
                }
            ),
            colors = TextFieldDefaults.colors(
                cursorColor = Color.White.copy(alpha = DefaultAlpha)
            )
        )
    }
}