/*
 * Copyright © Anton Sorokin 2025. All rights reserved
 */

package com.saionji.mysensor.ui

import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.saionji.mysensor.R
import com.saionji.mysensor.data.SettingsApp
import com.saionji.mysensor.data.SettingsSensor


@Composable
fun MainAppBar(
    optionsBoxState: OptionsBoxState,
    settingsItems: MutableList<SettingsSensor>,
    settingsApp: SettingsApp,
    onTextChange: (String) -> Unit,
    onAppSettingsChange: (SettingsApp) -> Unit,
    onSettingsChange: (List<SettingsSensor>) -> List<SettingsSensor>,
    onCloseClicked: () -> Unit,
    onAddClicked: () -> Unit,
    onRemoveClicked: (SettingsSensor) -> Unit,
    onEditSensorId: (Int, String) -> Unit,
    onEditSensorDescription: (Int, String) -> Unit,
    onDoneClicked: (List<SettingsSensor>, SettingsApp) -> Unit,
    onRefreshClicked: (List<SettingsSensor>) -> Unit,
    onOptionsBoxTriggered: () -> Unit,
    onAboutClicked: () -> Unit,
    onShareClicked: () -> Unit
) {
    when (optionsBoxState) {
        OptionsBoxState.CLOSED -> {
            ClosedAppBar (
                onSettingsClicked = onOptionsBoxTriggered,
                onRefreshClicked = onRefreshClicked,
                onAboutClicked = onAboutClicked,
                settingsItems = settingsItems,
                onShareClicked = onShareClicked
            )
        }
        OptionsBoxState.OPENED -> {
            ClosedAppBar (
                onSettingsClicked = onOptionsBoxTriggered,
                onRefreshClicked = onRefreshClicked,
                onAboutClicked = onAboutClicked,
                settingsItems = settingsItems,
                onShareClicked = onShareClicked
            )
            CustomDialog(
                settingsItems = settingsItems,
                settingsApp = settingsApp,
                onTextChange = onTextChange,
                onAppSettingsChange = onAppSettingsChange,
                onSettingsChange = onSettingsChange,
                onAddClicked = onAddClicked,
                onRemoveClicked = onRemoveClicked,
                onEditSensorId = onEditSensorId,
                onEditSensorDescription = onEditSensorDescription,
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
    onAboutClicked: () -> Unit,
    onShareClicked: () -> Unit,
    settingsItems: List<SettingsSensor>,
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
                    onShareClicked()
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.Share,
                    contentDescription = "ShareIcon"

                )
            }
            IconButton(
                onClick = { onAboutClicked() }
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = "AboutIcon"
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
