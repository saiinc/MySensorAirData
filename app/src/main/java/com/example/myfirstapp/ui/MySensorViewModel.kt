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
import com.example.myfirstapp.data.DataStoreManager
import com.example.myfirstapp.data.MySensor
import com.example.myfirstapp.data.MySensorRepository
import com.example.myfirstapp.data.SettingsData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.HttpRetryException
import kotlin.math.roundToInt

sealed interface MySensorUiState {
    data class Success(val getVal: List<MySensor>) : MySensorUiState
    object Error : MySensorUiState
    object Loading : MySensorUiState
}

class MySensorViewModel(
    private val mySensorRepository: MySensorRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    var mySensorUiState: MySensorUiState by mutableStateOf(MySensorUiState.Loading)
        private set

    private val _optionsBoxState: MutableState<OptionsBoxState> =
        mutableStateOf(value = OptionsBoxState.CLOSED)
    val optionsBoxState: State<OptionsBoxState> = _optionsBoxState

    private val _searchTextState : MutableState<String> =
        mutableStateOf(value = "")
    val searchTextState: State<String> = _searchTextState

    private val _sensorIdTextState : MutableState<String> =
        mutableStateOf(value = "")
    val sensorIdTextState: State<String> = _sensorIdTextState

    private val _historyItems : SnapshotStateList<String> = mutableStateListOf()
    val historyItems: SnapshotStateList<String> = _historyItems

    fun updateOptionsBoxState(newValue: OptionsBoxState) {
        _optionsBoxState.value = newValue
    }
    fun updateSensorIdTextState(newValue: String) {
        _sensorIdTextState.value = newValue
    }

    fun saveSensorId(ids: List<String>) {
        val newIds: List<String> = if (ids.size > 5)
            ids.slice(0..5)
            else ids
        viewModelScope.launch {
            dataStoreManager.saveSettings(
                SettingsData(
                    sensorId = "none",

                    sensorHistory = newIds.toSet()
                )
            )
        }
    }

    init {
        getSensorId()
    }
    private fun getSensorId() : String {
        var getId: String = "none1"
        viewModelScope.launch(Dispatchers.Main) {
            dataStoreManager.getSettings().collectLatest {
                getId = it.sensorId
                _historyItems.clear()
                _historyItems.addAll(it.sensorHistory.toMutableList())
                getMySensor(historyItems)
            }
        }
        return getId
    }

    fun resetSettings() {
        viewModelScope.launch(Dispatchers.Main) {
            dataStoreManager.getSettings().collectLatest {
                _historyItems.clear()
                _historyItems.addAll(it.sensorHistory.toMutableList())
            }
        }
    }

    private suspend fun getAllSensors(ids: List<String>) : MutableList<MySensor> {
        val allSensors = mutableListOf<MySensor>()
        for (id in ids) {
            val singleSensor = mySensorRepository.getSensor(id)
            for (sensorRecord in singleSensor) {
                val toDelete :List<String> = listOf("pressure_at_sealevel", "noise_LA_min", "noise_LA_max")
                if (sensorRecord.valueType !in toDelete) {
                    var toAddFlag = true
                    allSensors.replaceAll {
                        if (it.valueType == sensorRecord.valueType) {
                            toAddFlag = false
                            it.value = ((sensorRecord.value?.toDouble()
                                ?.let { it1 -> it.value?.toDouble()?.plus(it1) })?.div(2)).toString()
                        }
                        it
                    }
                    if (toAddFlag) {
                        allSensors.add(sensorRecord)
                    }
                }
            }
        }
        allSensors.forEach {
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
            allSensors[0].valueType = "Please tap the settings icon to add your sensor IDs."
        }
        return allSensors
    }

    fun getMySensor(ids: SnapshotStateList<String> ) {
        viewModelScope.launch {
            mySensorUiState = MySensorUiState.Loading
            mySensorUiState = try {
                MySensorUiState.Success(getAllSensors(ids))
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
                val dataStoreManager = DataStoreManager(application)

                MySensorViewModel(
                    mySensorRepository = mySensorRepository,
                    dataStoreManager= dataStoreManager
                )
            }
        }
    }
}

enum class OptionsBoxState {
    OPENED,
    CLOSED
}