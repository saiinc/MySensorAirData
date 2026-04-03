package com.saionji.mysensor.shared.viewmodel

import com.saionji.mysensor.shared.domain.model.LatLng
import com.saionji.mysensor.shared.domain.model.GetAddressFromCoordinatesUseCase
import com.saionji.mysensor.shared.domain.model.MapMarker
import com.saionji.mysensor.shared.domain.usecase.GetSensorValuesByAreaUseCase
import com.saionji.mysensor.shared.fake.FakeGeocodingRepository
import com.saionji.mysensor.shared.fake.FakeLocationService
import com.saionji.mysensor.shared.fake.FakeMySensorRepositoryForMap
import com.saionji.mysensor.shared.ui.map.MapUiState
import com.saionji.mysensor.shared.ui.map.SharedMapViewModel
import com.saionji.mysensor.shared.ui.map.model.MapBounds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class SharedMapViewModelTest {

    private lateinit var geocodingRepository: FakeGeocodingRepository
    private lateinit var sensorRepository: FakeMySensorRepositoryForMap
    private lateinit var locationService: FakeLocationService
    private lateinit var viewModel: SharedMapViewModel
    private val testScope = TestScope(UnconfinedTestDispatcher())

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        geocodingRepository = FakeGeocodingRepository()
        sensorRepository = FakeMySensorRepositoryForMap()
        locationService = FakeLocationService()

        viewModel = SharedMapViewModel(
            getAddressFromCoordinatesUseCase = GetAddressFromCoordinatesUseCase(geocodingRepository),
            getSensorValuesByAreaUseCase = GetSensorValuesByAreaUseCase(sensorRepository),
            locationService = locationService,
            scope = testScope
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ========== Marker Selection Tests ==========

    @Test
    fun `onMarkerSelected should update selectedMarker`() = runTest {
        // Given
        val marker = MapMarker(
            id = "123",
            lat = 55.75,
            lon = 37.60,
            valueType = "PM2.5",
            value = "10µg/m³",
            colorInt = 0xFF00FF00.toInt()
        )

        // When
        viewModel.onMarkerSelected(marker)

        // Then
        assertEquals(marker, viewModel.selectedMarker.value)
    }

    @Test
    fun `clearSelectedMarker should set selectedMarker to null`() = runTest {
        // Given
        val marker = MapMarker(
            id = "123",
            lat = 55.75,
            lon = 37.60,
            valueType = "PM2.5",
            value = "10µg/m³",
            colorInt = 0xFF00FF00.toInt()
        )
        viewModel.onMarkerSelected(marker)

        // When
        viewModel.clearSelectedMarker()

        // Then
        assertNull(viewModel.selectedMarker.value)
    }

    // ========== Location Tests ==========

    @Test
    fun `onLocationUpdated should update currentLocation`() = runTest {
        // Given
        val lat = 55.75
        val lon = 37.60

        // When
        viewModel.onLocationUpdated(lat, lon)

        // Then
        assertEquals(LatLng(lat, lon), viewModel.currentLocation.value)
    }

    @Test
    fun `onLocationUpdated first time should set camera position`() = runTest {
        // Given
        val lat = 55.75
        val lon = 37.60

        // When
        viewModel.onLocationUpdated(lat, lon)

        // Then
        val cameraState = viewModel.cameraState.value
        assertNotNull(cameraState)
        assertEquals(lat, cameraState?.lat)
        assertEquals(lon, cameraState?.lon)
        assertTrue(cameraState?.isProgrammatic == true)
    }

    @Test
    fun `onLocationUpdated second time should not change camera`() = runTest {
        // Given
        val lat1 = 55.75
        val lon1 = 37.60
        val lat2 = 56.00
        val lon2 = 38.00

        // First update
        viewModel.onLocationUpdated(lat1, lon1)
        val firstCameraState = viewModel.cameraState.value

        // When - second update
        viewModel.onLocationUpdated(lat2, lon2)

        // Then - camera should not change (initialCameraApplied = true)
        val secondCameraState = viewModel.cameraState.value
        assertEquals(firstCameraState, secondCameraState)
    }

    // ========== Value Type Tests ==========

    @Test
    fun `setSelectedValueType should update selectedValueType`() = runTest {
        // Given
        val newType = "PM10"

        // When
        viewModel.setSelectedValueType(newType)

        // Then
        assertEquals(newType, viewModel.selectedValueType.value)
    }

    @Test
    fun `setSelectedValueType same value should not trigger reload`() = runTest {
        // Given
        val type = "PM2.5"
        viewModel.setSelectedValueType(type)

        // Set some bounds first
        val bounds = MapBounds(56.0, 38.0, 55.0, 37.0, 10.0)
        viewModel.onViewportChanged(bounds)
        advanceUntilIdle()

        val initialCallCount = sensorRepository.callCount

        // When - set same type
        viewModel.setSelectedValueType(type)

        // Then - should not reload
        assertEquals(initialCallCount, sensorRepository.callCount)
    }

    // ========== Area Loading Tests ==========

    @Test
    fun `loadSensorsForArea with low zoom should not load`() = runTest {
        // Given
        val bounds = MapBounds(56.0, 38.0, 55.0, 37.0, zoom = 5.0)

        // When
        viewModel.loadSensorsForArea(bounds)

        // Then
        assertEquals(MapUiState.Idle, viewModel.mapUiState.value)
        assertEquals(0, sensorRepository.callCount)
    }

    @Test
    fun `loadSensorsForArea with valid bounds should load markers`() = runTest {
        // Given
        val marker = MapMarker(
            id = "123",
            lat = 55.75,
            lon = 37.60,
            valueType = "PM2.5",
            value = "10µg/m³",
            colorInt = 0xFF00FF00.toInt()
        )

        sensorRepository = FakeMySensorRepositoryForMap(
            markers = listOf(
                com.saionji.mysensor.shared.domain.model.MapSensor(
                    id = "123",
                    lat = 55.75,
                    lon = 37.60,
                    measurements = emptyList()
                )
            )
        )

        viewModel = SharedMapViewModel(
            getAddressFromCoordinatesUseCase = GetAddressFromCoordinatesUseCase(geocodingRepository),
            getSensorValuesByAreaUseCase = GetSensorValuesByAreaUseCase(sensorRepository),
            locationService = locationService,
            scope = testScope
        )

        val bounds = MapBounds(56.0, 38.0, 55.0, 37.0, zoom = 10.0)

        // When
        viewModel.loadSensorsForArea(bounds)
        advanceUntilIdle()

        // Then
        val state = viewModel.mapUiState.value
        assertTrue(state is MapUiState.Success)
    }

    @Test
    fun `loadSensorsForArea on error should set Error state`() = runTest {
        // Given
        sensorRepository = FakeMySensorRepositoryForMap(shouldFail = true)

        viewModel = SharedMapViewModel(
            getAddressFromCoordinatesUseCase = GetAddressFromCoordinatesUseCase(geocodingRepository),
            getSensorValuesByAreaUseCase = GetSensorValuesByAreaUseCase(sensorRepository),
            locationService = locationService,
            scope = testScope
        )

        val bounds = MapBounds(56.0, 38.0, 55.0, 37.0, zoom = 10.0)

        // When
        viewModel.loadSensorsForArea(bounds)
        advanceUntilIdle()

        // Then
        val state = viewModel.mapUiState.value
        assertTrue(state is MapUiState.Error)
    }

    // ========== Address Tests ==========

    @Test
    fun `ensureAddress should load address for coordinates`() = runTest {
        // Given
        val lat = 55.75
        val lon = 37.60

        // When
        viewModel.ensureAddress(lat, lon)
        advanceUntilIdle()

        // Then
        assertEquals(1, geocodingRepository.callCount)
        assertEquals(lat, geocodingRepository.lastLat)
        assertEquals(lon, geocodingRepository.lastLon)
    }

    @Test
    fun `ensureAddress should not reload existing address`() = runTest {
        // Given
        val lat = 55.75
        val lon = 37.60

        // First load
        viewModel.ensureAddress(lat, lon)
        advanceUntilIdle()

        // When - second load same coordinates
        viewModel.ensureAddress(lat, lon)
        advanceUntilIdle()

        // Then - should call only once
        assertEquals(1, geocodingRepository.callCount)
    }

    // ========== Build Settings Sensor Tests ==========

    @Test
    fun `buildSettingsSensorFromMap should create SettingsSensor`() = runTest {
        // Given
        val sensorId = "12345"
        val address = "Moscow, Russia"
        var result: com.saionji.mysensor.shared.data.model.SettingsSensor? = null

        // When
        viewModel.buildSettingsSensorFromMap(sensorId, address) {
            result = it
        }

        // Then
        assertNotNull(result)
        assertEquals(sensorId, result?.id)
        assertEquals(address, result?.description)
    }
}