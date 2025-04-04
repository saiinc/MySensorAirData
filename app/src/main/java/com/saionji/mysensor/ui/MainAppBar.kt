/*
 * Copyright © Anton Sorokin 2025. All rights reserved
 */

package com.saionji.mysensor.ui

import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.res.stringResource
import com.saionji.mysensor.R
import com.saionji.mysensor.data.SettingsApp
import com.saionji.mysensor.data.SettingsSensor


@Composable
fun MainAppBar(
    optionsBoxState: OptionsBoxState,
    sensorsOptions: State<List<SettingsSensor>>,
    settingsApp: SettingsApp,
    onTextChange: (String) -> Unit,
    onAppSettingsChange: (SettingsApp) -> Unit,
    onCloseClicked: () -> Unit,
    onAddClicked: () -> Unit,
    onRemoveClicked: (SettingsSensor) -> Unit,
    onEditSensorId: (Int, String) -> Unit,
    onEditSensorDescription: (Int, String) -> Unit,
    onDoneClicked: (State<List<SettingsSensor>>, SettingsApp) -> Unit,
    onOptionsBoxTriggered: () -> Unit,
    onAboutClicked: () -> Unit,
    onShareClicked: () -> Unit
) {
    when (optionsBoxState) {
        OptionsBoxState.CLOSED -> {
            ClosedAppBar (
                onSettingsClicked = onOptionsBoxTriggered,
                onAboutClicked = onAboutClicked,
                onShareClicked = onShareClicked
            )
        }
        OptionsBoxState.OPENED -> {
            ClosedAppBar (
                onSettingsClicked = onOptionsBoxTriggered,
                onAboutClicked = onAboutClicked,
                onShareClicked = onShareClicked
            )
            CustomDialog(
                sensorsOptions = sensorsOptions,
                settingsApp = settingsApp,
                onTextChange = onTextChange,
                onAppSettingsChange = onAppSettingsChange,
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
    onAboutClicked: () -> Unit,
    onShareClicked: () -> Unit,
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
