package com.example.myfirstapp.ui

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpRetryException
import java.util.stream.Collectors.toSet
import kotlin.math.hypot

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

    private var mySensorId: String = "none" //getSensorId()

    private val _searchWidgetState: MutableState<SearchWidgetState> =
        mutableStateOf(value = SearchWidgetState.CLOSED)
    val searchWidgetState: State<SearchWidgetState> = _searchWidgetState

    private val _searchTextState : MutableState<String> =
        mutableStateOf(value = "")
    val searchTextState: State<String> = _searchTextState

    var historyItems = mutableListOf("")

    fun updateSearchWidgetState(newValue: SearchWidgetState) {
        _searchWidgetState.value = newValue
    }

    fun updateSearchTextState(newValue: String) {
        _searchTextState.value = newValue
    }

    fun getSensorId() : String {
        viewModelScope.launch(Dispatchers.Main) {
            dataStoreManager.getSettings().collectLatest {
                mySensorId = it.sensorId
                historyItems = it.sensorHistory.toMutableList()
                getMySensor(mySensorId)
            }
        }
        return mySensorId
    }

    fun saveSensorId(saveId: String) {
        historyItems.add(0, saveId)
        if (historyItems.size > 5)
            historyItems = historyItems.slice(0..5).toMutableList()
        viewModelScope.launch {
            dataStoreManager.saveSettings(
                SettingsData(
                    sensorId = saveId,
                    sensorHistory = historyItems.toSet()
                )
            )
        }
        mySensorId = saveId
    }


    init {
        getSensorId()
        //getMySensor(getSensorId())
    }

    fun getMySensor(id: String ) { //getSensorId() 71128
        viewModelScope.launch {
            mySensorUiState = MySensorUiState.Loading
            mySensorUiState = try {
                MySensorUiState.Success(mySensorRepository.getSensor(id))
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

                MySensorViewModel(mySensorRepository = mySensorRepository, dataStoreManager)
            }
        }
    }
}

enum class SearchWidgetState {
    OPENED,
    CLOSED
}