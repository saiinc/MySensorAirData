package com.example.myfirstapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
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
    searchWidgetState: SearchWidgetState,
    historyItems: List<String>,
    searchTextState: String,
    onTextChange: (String) -> Unit,
    onCloseClicked: () -> Unit,
    onSearchClicked: (String) -> Unit,
    onSearchTriggered: () -> Unit
) {
    when (searchWidgetState) {
        SearchWidgetState.CLOSED -> {
            ClosedAppBar (
                onSearchClicked = onSearchTriggered
            )
        }
        SearchWidgetState.OPENED -> {
            OpenedAppBar(
                historyItems = historyItems,
                text = searchTextState,
                onTextChange = onTextChange,
                onCloseClicked = onCloseClicked,
                onSearchClicked = onSearchClicked,
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClosedAppBar(onSearchClicked: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(id = R.string.app_name)
            )
        },
        actions = {
            IconButton(
                onClick = {
                    onSearchClicked()
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "SearchIcon",
                    tint = Color.Black
                )
                
            }
        }
    )
}

@Composable
fun OpenedAppBar(
    historyItems: List<String>,
    text: String,
    onTextChange: (String) -> Unit,
    onCloseClicked: () -> Unit,
    onSearchClicked: (String) -> Unit
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
                    onClick = { onSearchClicked(text) })
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
                    onSearchClicked(text)
                }
            ),
            colors = TextFieldDefaults.colors(
                cursorColor = Color.White.copy(alpha = DefaultAlpha)
            )
        )
    }
    LazyColumn (
        modifier = Modifier
            .padding(start = 32.dp, top = 54.dp)
    ) {
        items(historyItems) {historyItem ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable{onTextChange(historyItem)}
            ) {
                Text(
                    text = historyItem,
                    //modifier = Modifier.clickable { onSearchClicked(text) },
                    modifier = Modifier.padding(
                        start = 8.dp,
                        top = 4.dp,
                        end = 8.dp,
                        bottom = 4.dp)
                )
            }
        }
    }
}