package com.example.myfirstapp.ui

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myfirstapp.MySensorApplication
import com.example.myfirstapp.data.MyDevice
import com.example.myfirstapp.data.MySensor
import com.example.myfirstapp.data.MySensorRepository
import com.example.myfirstapp.data.SettingsRepository
import com.example.myfirstapp.data.SettingsSensor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.HttpRetryException
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.roundToInt

sealed interface MySensorUiState {
    data class Success(val getVal: List<MyDevice>) : MySensorUiState
    object Error : MySensorUiState
    object Loading : MySensorUiState
}

class MySensorViewModel(
    private val mySensorRepository: MySensorRepository,
    private val settingsRepository: SettingsRepository
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

    private suspend fun getAllSensors(devices: List<SettingsSensor>) : List<MyDevice> {
        //val allSensors = mutableListOf<MySensor>()
        val allDevices = mutableListOf<MyDevice>()
        val devicesClone = devices.toList()
        for (device in devicesClone) {
            val singleSensor = mySensorRepository.getSensor(device.id)
            val singleSensorCopy = CopyOnWriteArrayList(singleSensor)
            singleSensorCopy.forEach {
                when (it.valueType) {
                    "P1" -> {it.valueType = "PM10"; it.value = "${it.value}µg/m³"}
                    "P2" -> {it.valueType = "PM2.5"; it.value = "${it.value}µg/m³"}
                    "temperature" -> it.value = "${it.value?.toDouble()?.roundToInt()}°C"
                    "humidity" -> it.value = "${it.value?.toDouble()?.roundToInt()}% RH"
                    "noise_LAeq" -> {it.valueType = "noise LAeq"; it.value = "${it.value}dBA"}
                    "pressure" -> it.value = "${it.value?.toDouble()?.div(100)?.roundToInt()}hPA"
                    "pressure_at_sealevel" -> singleSensorCopy.remove(it)
                }
            }
            allDevices.add(MyDevice(id = device.id, description = device.description, deviceSensors = singleSensorCopy))
            /*for (sensorRecord in singleSensor) {
                val toDelete :List<String> = listOf("pressure_at_sealevel", "noise_LA_min", "noise_LA_max")
                if (sensorRecord.valueType !in toDelete) {
                    var toAddFlag = true
                    allSensors.replaceAll {
                        if (it.valueType == sensorRecord.valueType) {
                            toAddFlag = false
                            val myDoubleValue = (sensorRecord.value?.toDouble()
                                ?.let { it1 -> it.value?.toDouble()?.plus(it1) })?.div(2)
                            if (myDoubleValue != null) {
                                it.value = ((myDoubleValue * 100).roundToInt() / 100.0).toString()
                            }
                        }
                        it
                    }
                    if (toAddFlag) {
                        allSensors.add(sensorRecord)
                    }
                }
            }*/
        }
        /*allSensors.forEach {
            when (it.valueType) {
                "P1" -> {it.valueType = "PM10"; it.value = "${it.value}µg/m³"}
                "P2" -> {it.valueType = "PM2.5"; it.value = "${it.value}µg/m³"}
                "temperature" -> it.value = "${it.value?.toDouble()?.roundToInt()}°C"
                "humidity" -> it.value = "${it.value?.toDouble()?.roundToInt()}% RH"
                "noise_LAeq" -> {it.valueType = "noise LAeq"; it.value = "${it.value}dBA"}
                "pressure" -> it.value = "${it.value?.toDouble()?.div(100)?.roundToInt()}hPA"
            }
        }
        if ((allSensors.size == 1) and (allSensors[0].valueType == "Sensor \"\" not found")) {
            allSensors[0].valueType = "Please tap the settings icon (⚙) to add your sensor IDs."
        }*/
        if ((allDevices.size == 1) and (allDevices[0].deviceSensors[0].valueType == "Sensor \"\" not found")) {
            allDevices[0].deviceSensors[0].valueType = "Please tap the settings icon (⚙) to add your sensor IDs."
        }
        return allDevices
    }

    fun getMySensor(devices: List<SettingsSensor> ) {
        viewModelScope.launch {
            mySensorUiState = MySensorUiState.Loading
            mySensorUiState = try {
                MySensorUiState.Success(getAllSensors(devices))
            } catch (e: IOException) {
                MySensorUiState.Error
            } catch (e: HttpRetryException) {
                MySensorUiState.Error
            }
        }
    }

    companion object{
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as MySensorApplication)
                val mySensorRepository = application.container.mySensorRepository
                val settingsRepository = SettingsRepository(application)

                MySensorViewModel(
                    mySensorRepository = mySensorRepository,
                    settingsRepository = settingsRepository

                )
            }
        }
    }
}

enum class OptionsBoxState {
    OPENED,
    CLOSED
}