package com.example.myfirstapp.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myfirstapp.data.MyDevice
import com.example.myfirstapp.data.MySensor


@Composable
fun SensorsGridScreen(
    sensors: List<MySensor>,
    modifier: Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(350.dp),
        contentPadding = PaddingValues(4.dp),
    ) {
        itemsIndexed(sensors) { _, mySensor ->
            MySensorData(mySensor = mySensor)

        }
    }
}

@Composable
fun MyGrid(
    modifier: Modifier, myListInt: List<Int>
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(150.dp),
        contentPadding = PaddingValues(4.dp),
    ) {
        itemsIndexed(myListInt) { _, int ->
            MyCard(int1 = int)
        }
    }
}

@Composable
fun MyCard(
    modifier: Modifier = Modifier,
    int1: Int
){
    Card(
        modifier = modifier
            .padding(4.dp)
            .fillMaxWidth()
            .requiredHeight(116.dp),

        ) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = int1.toString(),
                textAlign = TextAlign.Center,
                fontSize = 21.sp,
                modifier = modifier
                    .padding(top = 8.dp, bottom = 8.dp)
            )
            Text(
                text = int1.toString(),
                textAlign = TextAlign.Center,
                fontSize = 21.sp,
                modifier = modifier
                    .padding(top = 8.dp, bottom = 8.dp)
            )


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
            //.fillMaxHeight()
            .requiredHeight(334.dp),
        colors = CardDefaults.cardColors(containerColor = Color.LightGray)

    ) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                mySensor.valueType?.let {
                    Text(
                        text = it,
                        textAlign = TextAlign.Center,
                        fontSize = 21.sp,
                        modifier = modifier
                            .padding(top = 8.dp, bottom = 8.dp)
                    )
                }
                mySensor.value?.let {
                    Text(
                        text = it,
                        textAlign = TextAlign.Center,
                        modifier = modifier
                            .padding(top = 4.dp, bottom = 8.dp)
                    )
                }
        }
        val myListInt: List<Int> = mutableListOf(1,2,3)
        MyGrid(modifier = modifier, myListInt)
    }
}