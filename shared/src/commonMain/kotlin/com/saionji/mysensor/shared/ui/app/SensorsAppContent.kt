package com.saionji.mysensor.shared.ui.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.saionji.mysensor.shared.ui.components.ErrorBanner
import com.saionji.mysensor.shared.ui.components.MainAppBar
import com.saionji.mysensor.shared.ui.components.OptionsBoxState
import com.saionji.mysensor.shared.ui.screens.AboutScreen
import com.saionji.mysensor.shared.ui.screens.HomeScreen
import com.saionji.mysensor.shared.ui.screens.MainScreenContent
import com.saionji.mysensor.shared.ui.screens.ShareScreen
import com.saionji.mysensor.shared.ui.viewmodel.MySensorViewModel
import com.saionji.mysensor.shared.ui.viewmodel.Screen
import com.saionji.mysensor.shared.ui.navigation.Main
import com.saionji.mysensor.shared.ui.navigation.About
import org.jetbrains.compose.resources.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.saionji.mysensor.shared.generated.resources.Res
import com.saionji.mysensor.shared.generated.resources.error_loading
import com.saionji.mysensor.shared.generated.resources.error_unknown
import androidx.compose.ui.graphics.ImageBitmap

/**
 * Кроссплатформенный контент приложения с NavHost
 *
 * Принимает ViewModel напрямую и собирает состояния внутри.
 * Платформенно-специфичные компоненты передаются через слоты.
 *
 * @param mySensorViewModel Shared ViewModel (из shared модуля)
 * @param mapScreen Слот для MapScreen (платформенно-специфичный)
 * @param permissionHandler Слот для обработки permissions (платформенно-специфичный)
 * @param onShare Callback для шаринга (платформенно-специфичный)
 */
@Composable
fun SensorsAppContent(
    mySensorViewModel: MySensorViewModel,
    mapScreen: @Composable () -> Unit,
    permissionHandler: @Composable () -> Unit,
    onShare: (ImageBitmap?) -> Unit,
    modifier: Modifier = Modifier
) {
    // ✅ Сбор состояний внутри shared (было в Android wrapper)
    val currentScreen by mySensorViewModel.currentScreen.collectAsState()
    val optionsBoxState = mySensorViewModel.optionsBoxState
    val settingsApp = mySensorViewModel.settingsApp.value
    val settingsItems = mySensorViewModel.dashboardItems.collectAsState()
    val sensorsOptions = mySensorViewModel.sensorsOptions.collectAsState()
    val isRefreshing = mySensorViewModel.isRefreshing.collectAsState().value
    val error by mySensorViewModel.error.collectAsState()
    val showShareScreen = mySensorViewModel.showShareScreen.value

    // ✅ KMP NavController
    val navController = rememberNavController()

    // Подписка на навигационные события
    LaunchedEffect(Unit) {
        mySensorViewModel.navigationEvent.collect { destination ->
            navController.navigate(destination)
        }
    }

    // ✅ Обработка ошибок (было в Android wrapper)
    val errorMessage = when (error) {
        MySensorViewModel.ErrorType.Network -> stringResource(Res.string.error_loading)
        MySensorViewModel.ErrorType.Unknown -> stringResource(Res.string.error_unknown)
        null -> null
    }

    if (errorMessage != null) {
        ErrorBanner(message = errorMessage)
    }

    // ✅ NavHost с типизированными роутами
    NavHost(
        navController = navController,
        startDestination = Main
    ) {
        composable<Main> {
            MainScreenContent(
                currentScreen = currentScreen,
                topBar = {
                    MainAppBar(
                        optionsBoxState = optionsBoxState.value,
                        sensorsOptions = sensorsOptions,
                        settingsApp = settingsApp,
                        onTextChange = { mySensorViewModel.updateSensorIdTextState(newValue = it) },
                        onAppSettingsChange = { mySensorViewModel.updateSettingsAppState(newValue = it) },
                        onCloseClicked = {
                            mySensorViewModel.resetAppSettings()
                            mySensorViewModel.updateOptionsBoxState(newValue = OptionsBoxState.CLOSED)
                        },
                        onDoneClicked = { sensorsOpts, settings ->
                            mySensorViewModel.saveAppSettings(settings)
                            val updatedSensors = sensorsOpts.value.map { it.copy() }
                            mySensorViewModel.saveSensors(updatedSensors)
                            mySensorViewModel.getDeviceSensors()
                            mySensorViewModel.updateOptionsBoxState(newValue = OptionsBoxState.CLOSED)
                        },
                        onAddClicked = { mySensorViewModel.addSensorOptions() },
                        onRemoveClicked = { mySensorViewModel.removeSensorOptions(it) },
                        onEditSensorId = { index, id ->
                            mySensorViewModel.updateSensorOptionsItemId(index, id)
                        },
                        onEditSensorDescription = { index, desc ->
                            mySensorViewModel.updateSensorOptionsItemDescription(index, desc)
                        },
                        onOptionsBoxTriggered = {
                            mySensorViewModel.sensorsOptionsLoad()
                            mySensorViewModel.updateOptionsBoxState(newValue = OptionsBoxState.OPENED)
                        },
                        onAboutClicked = { mySensorViewModel.navigateTo(About) },
                        onShareClicked = { mySensorViewModel.setShowShareScreen(true) }
                    )
                },
                homeScreen = {
                    HomeScreen(
                        dashboardSensor = settingsItems,
                        isRefreshing = isRefreshing,
                        onRefresh = mySensorViewModel::refresh
                    )
                },
                mapScreen = {
                    permissionHandler()
                    mapScreen()
                },
                onDashboardClick = { mySensorViewModel.switchToScreen(Screen.Dashboard) },
                onMapClick = { mySensorViewModel.switchToScreen(Screen.Map) },
                modifier = modifier
            )
        }

        composable<About> {
            AboutScreen(
                onBackClicked = { navController.popBackStack() }
            )
        }
    }

    // ✅ ShareScreen (было в Android wrapper)
    if (showShareScreen) {
        ShareScreen(
            settingsApp = settingsApp,
            settingsItems = settingsItems,
            onImageGenerated = { image ->
                onShare(image)
                mySensorViewModel.setShowShareScreen(false)
            }
        )
    }
}