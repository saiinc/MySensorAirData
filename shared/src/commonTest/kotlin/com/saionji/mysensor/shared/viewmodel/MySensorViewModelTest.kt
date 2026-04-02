package com.saionji.mysensor.shared.viewmodel

import app.cash.turbine.test
import com.saionji.mysensor.shared.data.model.SettingsSensor
import com.saionji.mysensor.shared.domain.usecase.GetSensorValuesUseCase
import com.saionji.mysensor.shared.fake.FakeMySensorRepository
import com.saionji.mysensor.shared.fake.FakeSettingsRepository
import com.saionji.mysensor.shared.ui.components.OptionsBoxState
import com.saionji.mysensor.shared.ui.viewmodel.MySensorViewModel
import com.saionji.mysensor.shared.ui.viewmodel.Screen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class MySensorViewModelTest {

    private lateinit var settingsRepository: FakeSettingsRepository
    private lateinit var mySensorRepository: FakeMySensorRepository
    private lateinit var getSensorValuesUseCase: GetSensorValuesUseCase
    private lateinit var viewModel: MySensorViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        settingsRepository = FakeSettingsRepository()
        mySensorRepository = FakeMySensorRepository()
        getSensorValuesUseCase = GetSensorValuesUseCase(mySensorRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        initialSettings: List<SettingsSensor> = emptyList(),
        shouldFailNetwork: Boolean = false
    ): MySensorViewModel {
        settingsRepository = FakeSettingsRepository(initialSettings = initialSettings)
        mySensorRepository = FakeMySensorRepository(shouldFail = shouldFailNetwork)
        getSensorValuesUseCase = GetSensorValuesUseCase(mySensorRepository)

        return MySensorViewModel(
            settingsRepository = settingsRepository,
            getSensorValuesUseCase = getSensorValuesUseCase
        )
    }

    @Test
    fun `initLoad should load saved sensors from repository`() = runTest {
        // Given - есть сохранённые настройки
        val savedSettings = listOf(
            SettingsSensor(id = "12345", description = "Home"),
            SettingsSensor(id = "67890", description = "Office")
        )

        // When - создаём ViewModel
        viewModel = createViewModel(initialSettings = savedSettings)

        // Then - dashboardItems содержит сохранённые сенсоры
        viewModel.dashboardItems.test {
            val items = awaitItem()
            assertEquals(2, items.size)
            assertEquals("12345", items[0].id)
            assertEquals("Home", items[0].description)
            assertEquals("67890", items[1].id)
            assertEquals("Office", items[1].description)
        }
    }

    @Test
    fun `switchToScreen should update currentScreen`() = runTest {
        // Given
        viewModel = createViewModel()

        // Initial state
        assertEquals(Screen.Dashboard, viewModel.currentScreen.value)

        // When
        viewModel.switchToScreen(Screen.Map)

        // Then
        assertEquals(Screen.Map, viewModel.currentScreen.value)

        // And back
        viewModel.switchToScreen(Screen.Dashboard)
        assertEquals(Screen.Dashboard, viewModel.currentScreen.value)
    }

    @Test
    fun `refresh should update isRefreshing state`() = runTest {
        // Given
        viewModel = createViewModel(
            initialSettings = listOf(SettingsSensor(id = "123", description = "Test"))
        )

        // When & Then
        viewModel.isRefreshing.test {
            assertFalse(awaitItem()) // Initial: false

            viewModel.refresh()

            // Может пропустить промежуточные состояния из-за timing
            // Проверяем финальное состояние
            skipItems(1) // Пропускаем true

            // Ждём false после завершения
            val finalState = awaitItem()
            assertFalse(finalState)
        }
    }

    @Test
    fun `getDeviceSensors should set error on network failure`() = runTest {
        // Given - repository выбрасывает ошибку
        viewModel = createViewModel(
            initialSettings = listOf(SettingsSensor(id = "123", description = "Test")),
            shouldFailNetwork = true
        )

        // When - загружаем данные
        viewModel.getDeviceSensors()

        // Then - ошибка установлена
        viewModel.error.test {
            val error = awaitItem()
            assertNotNull(error)
            assertEquals(MySensorViewModel.ErrorType.Network, error)
        }
    }

    @Test
    fun `getDeviceSensors should update dashboardItems with sensor data`() = runTest {
        // Given
        viewModel = createViewModel(
            initialSettings = listOf(SettingsSensor(id = "123", description = "Home"))
        )

        advanceUntilIdle()  // Ждём завершения initLoad() и getDeviceSensors()

        // Then - данные уже загружены через initLoad
        val items = viewModel.dashboardItems.value
        assertEquals(1, items.size)
        assertEquals("123", items[0].id)
        assertFalse(items[0].isLoading)
        assertTrue(items[0].deviceSensors.isNotEmpty())

        // And - repository был вызван через initLoad
        assertEquals(1, mySensorRepository.callCount)
        assertEquals("123", mySensorRepository.lastSensorId)
    }

    @Test
    fun `saveSensors should call repository`() = runTest {
        // Given
        viewModel = createViewModel()
        val sensors = listOf(
            SettingsSensor(id = "111", description = "Sensor 1"),
            SettingsSensor(id = "222", description = "Sensor 2")
        )

        // When
        viewModel.saveSensors(sensors)

        // Then
        assertEquals(1, settingsRepository.saveSettingsCallCount)
        assertEquals(sensors, settingsRepository.lastSavedSettings)
    }

    @Test
    fun `addSensorDashboardFromMap should add new sensor to dashboard`() = runTest {
        // Given
        viewModel = createViewModel()
        val newSensor = SettingsSensor(id = "99999", description = "From Map")

        // When
        viewModel.addSensorDashboardFromMap(newSensor)

        // Then
        viewModel.dashboardItems.test {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals("99999", items[0].id)
            assertEquals("From Map", items[0].description)
        }
    }

    @Test
    fun `removeSensorDashboardFromMap should remove sensor from dashboard`() = runTest {
        // Given
        viewModel = createViewModel(
            initialSettings = listOf(
                SettingsSensor(id = "111", description = "Sensor 1"),
                SettingsSensor(id = "222", description = "Sensor 2")
            )
        )

        // When
        viewModel.removeSensorDashboardFromMap("111")

        // Then
        viewModel.dashboardItems.test {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals("222", items[0].id)
        }
    }

    @Test
    fun `updateOptionsBoxState should change optionsBoxState`() = runTest {
        // Given
        viewModel = createViewModel()

        // Initial state
        assertEquals(OptionsBoxState.CLOSED, viewModel.optionsBoxState.value)

        // When
        viewModel.updateOptionsBoxState(OptionsBoxState.OPENED)

        // Then
        assertEquals(OptionsBoxState.OPENED, viewModel.optionsBoxState.value)
    }
}


