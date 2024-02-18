package com.example.myfirstapp.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myfirstapp.data.MySensor
import com.example.myfirstapp.ui.MySensorUiState
import com.example.myfirstapp.ui.MySensorViewModel

@Composable
fun HomeScreen(
    mySensorUiState: MySensorUiState,
    retryAction: () -> Unit,
    modifier: Modifier
) {
    when (mySensorUiState) {
        is MySensorUiState.Loading -> LoadingScreen(modifier)
        is MySensorUiState.Success -> SensorsGridScreen(
            sensors = mySensorUiState.getVal,
            modifier = modifier,
        )
        is MySensorUiState.Error -> ErrorScreen(retryAction = retryAction, modifier)
    }
}