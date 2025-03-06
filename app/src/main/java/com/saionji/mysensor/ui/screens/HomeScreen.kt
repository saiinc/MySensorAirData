/*
 * Copyright © Anton Sorokin 2025. All rights reserved
 */

package com.saionji.mysensor.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.saionji.mysensor.ui.MySensorUiState

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
        is MySensorUiState.Error -> ErrorScreen(retryAction = retryAction, modifier)
    }
}