package com.example.myfirstapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Updater
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myfirstapp.R
import com.example.myfirstapp.data.MySensor


@Composable
fun SensorsGridScreen(
    sensors: List<MySensor>,
    modifier: Modifier
) {

    LazyVerticalGrid(
        columns = GridCells.Adaptive(150.dp),
        contentPadding = PaddingValues(4.dp),
    ) {
        itemsIndexed(sensors) { _, mySensor ->
            MySensorData(mySensor = mySensor)
        }
    }
}

@Composable
fun MySensorData(
    modifier: Modifier = Modifier,
    mySensor: MySensor
) {
    Card(
        modifier = modifier
            .padding(4.dp)
            .fillMaxWidth()
            .requiredHeight(296.dp),

    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            mySensor.valueType?.let {
                Text(
                    text = it,
                    textAlign = TextAlign.Center,
                    fontSize = 21.sp,
                    modifier = modifier
                        .padding(start= 8.dp, top = 4.dp, bottom = 8.dp))
            }
            mySensor.value?.let {
                Text(
                    text = it,
                    textAlign = TextAlign.Center,
                    modifier = modifier
                        .padding(top = 4.dp, bottom = 8.dp))
            }
        }
    }
}