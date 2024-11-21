/*
 * Copyright © Anton Sorokin 2024. All rights reserved
 */

package com.saionji.mysensor.ui

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.saionji.mysensor.MySensorApplication
import com.saionji.mysensor.data.MyDevice
import com.saionji.mysensor.data.SettingsRepository
import com.saionji.mysensor.data.SettingsSensor
import com.saionji.mysensor.domain.GetAllSensorsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.net.HttpRetryException

sealed interface MySensorUiState {
    data class Success(val getVal: List<MyDevice>) : MySensorUiState
    object Error : MySensorUiState
    object Loading : MySensorUiState
}

class MySensorViewModel(
    private val settingsRepository: SettingsRepository,
    private val getAllSensorsUseCase: GetAllSensorsUseCase
) : ViewModel() {

    var mySensorUiState: MySensorUiState by mutableStateOf(MySensorUiState.Loading)
        private set

    private val _optionsBoxState: MutableState<OptionsBoxState> =
        mutableStateOf(value = OptionsBoxState.CLOSED)
    val optionsBoxState: State<OptionsBoxState> = _optionsBoxState

    private val _sensorIdTextState : MutableState<String> =
        mutableStateOf(value = "")
    val sensorIdTextState: State<String> = _sensorIdTextState

    private val _settingsItems : MutableList<SettingsSensor> = mutableStateListOf()
    val settingsItems: MutableList<SettingsSensor> = _settingsItems

    private val _navigationEvent = MutableSharedFlow<String>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    fun navigateTo(screen: String) {
        viewModelScope.launch {
            _navigationEvent.emit(screen)
        }
    }

    private val _showShareScreen = mutableStateOf(false)
    val showShareScreen: State<Boolean> = _showShareScreen

    fun setShowShareScreen(value: Boolean) {
        _showShareScreen.value = value
    }

    fun updateSettingsItems(newValue: List<SettingsSensor>) : List<SettingsSensor>{
        val list = mutableListOf<SettingsSensor>()
        list.addAll(newValue)
        settingsItems.clear()
        settingsItems.addAll(list)
        return settingsItems
    }

    fun updateOptionsBoxState(newValue: OptionsBoxState) {
        _optionsBoxState.value = newValue
    }
    fun updateSensorIdTextState(newValue: String) {
        _sensorIdTextState.value = newValue
    }

    fun saveSettings(mySettings: List<SettingsSensor>) {
        viewModelScope.launch {
            settingsRepository.saveSettings(mySettings)
        }
    }

    init {
        initLoad()
    }
    private fun initLoad() {
        viewModelScope.launch(Dispatchers.Main) {
            settingsRepository.getSettings().collectLatest {mySettings ->
                _settingsItems.clear()
                _settingsItems.addAll(mySettings)
                getMySensor(_settingsItems)
            }
        }
    }

    fun resetSettings() {
        viewModelScope.launch(Dispatchers.Main) {
            settingsRepository.getSettings().collectLatest {
                _settingsItems.clear()
                _settingsItems.addAll(it)
            }
        }
    }

    fun getMySensor(devices: List<SettingsSensor> ) {
        viewModelScope.launch {
            mySensorUiState = MySensorUiState.Loading
            mySensorUiState = try {
                MySensorUiState.Success(getAllSensorsUseCase(devices))
            } catch (e: IOException) {
                MySensorUiState.Error
            } catch (e: HttpRetryException) {
                MySensorUiState.Error
            } catch (e: HttpException) {
                MySensorUiState.Error
            }
        }
    }

    companion object{
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as MySensorApplication)
                val settingsRepository = application.container.settingsRepository
                val getAllSensorsUseCase = application.container.getAllSensorsUseCase

                MySensorViewModel(
                    settingsRepository = settingsRepository,
                    getAllSensorsUseCase = getAllSensorsUseCase

                )
            }
        }
    }
}

enum class OptionsBoxState {
    OPENED,
    CLOSED
}