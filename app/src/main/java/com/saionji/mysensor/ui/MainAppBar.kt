/*
 * Copyright © Anton Sorokin 2024. All rights reserved
 */

package com.saionji.mysensor.ui

import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.saionji.mysensor.R
import com.saionji.mysensor.data.SettingsSensor


@Composable
fun MainAppBar(
    optionsBoxState: OptionsBoxState,
    settingsItems: MutableList<SettingsSensor>,
    onTextChange: (String) -> Unit,
    onSettingsChange: (List<SettingsSensor>) -> List<SettingsSensor>,
    onCloseClicked: () -> Unit,
    onDoneClicked: (List<SettingsSensor>) -> Unit,
    onRefreshClicked: (List<SettingsSensor>) -> Unit,
    onOptionsBoxTriggered: () -> Unit
) {
    when (optionsBoxState) {
        OptionsBoxState.CLOSED -> {
            ClosedAppBar (
                onSettingsClicked = onOptionsBoxTriggered,
                onRefreshClicked = onRefreshClicked,
                settingsItems = settingsItems
            )
        }
        OptionsBoxState.OPENED -> {
            ClosedAppBar (
                onSettingsClicked = onOptionsBoxTriggered,
                onRefreshClicked = onRefreshClicked,
                settingsItems = settingsItems
            )
            CustomDialog(
                settingsItems = settingsItems,
                onTextChange = onTextChange,
                onSettingsChange = onSettingsChange,
                onDoneClicked = onDoneClicked,
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
    onSettingsClicked: () -> Unit,
    onRefreshClicked: (List<SettingsSensor>) -> Unit,
    settingsItems: List<SettingsSensor>
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
                    onRefreshClicked(settingsItems)
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "RefreshIcon"

                )
            }
            IconButton(
                onClick = {
                    onSettingsClicked()
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "SettingsIcon"
                )
                
            }
        }
    )
}
