/*
 * Copyright © Anton Sorokin 2025. All rights reserved
 */

package com.saionji.mysensor.ui.screens

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.saionji.mysensor.data.DashboardSensor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    dashboardSensor: State<List<DashboardSensor>>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    val pullRefreshState = rememberPullToRefreshState()
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        state = pullRefreshState,
        onRefresh = onRefresh,
    ) {
        PollutionDashboard(
            modifier = Modifier.pullToRefresh(
                isRefreshing = isRefreshing,
                state = pullRefreshState,
                threshold = 60.dp,
                onRefresh = onRefresh
            ),
            pollutionDataList = dashboardSensor
        )
    }
}