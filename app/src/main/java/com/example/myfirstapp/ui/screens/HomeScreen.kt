package com.example.myfirstapp.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.myfirstapp.ui.MySensorUiState

@Composable
fun HomeScreen(
    mySensorUiState: MySensorUiState,
    retryAction: () -> Unit,
    modifier: Modifier
) {
    when (mySensorUiState) {
        is MySensorUiState.Loading -> LoadingScreen(modifier)
        is MySensorUiState.Success -> PollutionDashboard(
            pollutionDataList = mySensorUiState.getVal
        )
        /*is MySensorUiState.Success -> SensorsGridScreen(
            sensors = mySensorUiState.getVal,
            modifier = modifier,
        )*/
        is MySensorUiState.Error -> ErrorScreen(retryAction = retryAction, modifier)
    }
}