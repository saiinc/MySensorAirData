package com.saionji.mysensor.shared.domain.usecase

import com.saionji.mysensor.shared.data.MySensor
import com.saionji.mysensor.shared.data.model.SettingsSensor
import com.saionji.mysensor.shared.fake.FakeMySensorRepository
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class GetSensorValuesUseCaseTest {

    private fun createUseCase(
        sensors: List<MySensor>
    ): GetSensorValuesUseCase {
        val repository = FakeMySensorRepository(
            customResult = sensors
        )

        return GetSensorValuesUseCase(repository)
    }

    @Test
    fun `invoke should normalize API sensor types`() = runTest {
        val useCase = createUseCase(
            listOf(
                MySensor(valueType = "P0", value = "1.2"),
                MySensor(valueType = "P1", value = "10.5"),
                MySensor(valueType = "P2", value = "5.7"),
                MySensor(valueType = "noise_LAeq", value = "42.0")
            )
        )

        val result = useCase(
            SettingsSensor(
                id = "123",
                description = "Test sensor"
            )
        )

        assertEquals(
            listOf("PM1", "PM2.5", "PM10", "noise LAeq"),
            result.map { it.valueType }
        )
    }

    @Test
    fun `invoke should exclude sea level pressure`() = runTest {
        val useCase = createUseCase(
            listOf(
                MySensor(
                    valueType = "pressure_at_sealevel",
                    value = "101325"
                ),
                MySensor(
                    valueType = "pressure",
                    value = "101325"
                )
            )
        )

        val result = useCase(
            SettingsSensor(
                id = "123",
                description = "Test sensor"
            )
        )

        assertEquals(1, result.size)
        assertEquals("pressure", result.single().valueType)
    }

    @Test
    fun `invoke should round temperature and humidity`() = runTest {
        val useCase = createUseCase(
            listOf(
                MySensor(
                    valueType = "temperature",
                    value = "21.6"
                ),
                MySensor(
                    valueType = "humidity",
                    value = "47.4"
                )
            )
        )

        val result = useCase(
            SettingsSensor(
                id = "123",
                description = "Test sensor"
            )
        )

        val byType = result.associateBy { it.valueType }

        assertEquals("22°C", byType["temperature"]?.value)
        assertEquals("47% RH", byType["humidity"]?.value)
    }

    @Test
    fun `invoke should convert pressure to hPA`() = runTest {
        val useCase = createUseCase(
            listOf(
                MySensor(
                    valueType = "pressure",
                    value = "101325"
                )
            )
        )

        val result = useCase(
            SettingsSensor(
                id = "123",
                description = "Test sensor"
            )
        )

        assertEquals("1013hPA", result.single().value)
    }

    @Test
    fun `invoke should sort sensors by predefined order`() = runTest {
        val useCase = createUseCase(
            listOf(
                MySensor(valueType = "noise_LAeq", value = "40"),
                MySensor(valueType = "humidity", value = "50"),
                MySensor(valueType = "PM10", value = "20"),
                MySensor(valueType = "temperature", value = "20"),
                MySensor(valueType = "PM1", value = "5"),
                MySensor(valueType = "pressure", value = "101000"),
                MySensor(valueType = "PM2.5", value = "10")
            )
        )

        val result = useCase(
            SettingsSensor(
                id = "123",
                description = "Test sensor"
            )
        )

        assertEquals(
            listOf(
                "PM1",
                "PM2.5",
                "PM10",
                "temperature",
                "humidity",
                "pressure",
                "noise LAeq"
            ),
            result.map { it.valueType }
        )
    }

    @Test
    fun `invoke should keep unknown sensor types after known types`() = runTest {
        val useCase = createUseCase(
            listOf(
                MySensor(valueType = "custom_value", value = "123"),
                MySensor(valueType = "temperature", value = "20")
            )
        )

        val result = useCase(
            SettingsSensor(
                id = "123",
                description = "Test sensor"
            )
        )

        assertEquals(
            listOf("temperature", "custom_value"),
            result.map { it.valueType }
        )
    }

    @Test
    fun `invoke should return empty list when repository returns no values`() = runTest {
        val useCase = createUseCase(emptyList())

        val result = useCase(
            SettingsSensor(
                id = "123",
                description = "Test sensor"
            )
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun `invoke should propagate repository failure`() = runTest {
        val repository = FakeMySensorRepository(
            shouldFail = true
        )
        val useCase = GetSensorValuesUseCase(repository)

        assertFailsWith<IOException> {
            useCase(
                SettingsSensor(
                    id = "123",
                    description = "Test sensor"
                )
            )
        }
    }

    @Test
    fun `invoke should skip invalid numeric value and keep valid sensors`() = runTest {
        val useCase = createUseCase(
            listOf(
                MySensor(
                    valueType = "P1",
                    value = "not-a-number"
                ),
                MySensor(
                    valueType = "P2",
                    value = "not-a-number"
                ),
                MySensor(
                    valueType = "temperature",
                    value = "not-a-number"
                ),
                MySensor(
                    valueType = "humidity",
                    value = "not-a-number"
                ),
                MySensor(
                    valueType = "noise_LAeq",
                    value = "not-a-number"
                ),
                MySensor(
                    valueType = "pressure",
                    value = "not-a-number"
                ),
                MySensor(
                    valueType = "humidity",
                    value = "45"
                )
            )
        )

        val result = useCase(
            SettingsSensor(
                id = "123",
                description = "Test sensor"
            )
        )

        assertEquals(listOf("humidity"), result.map { it.valueType })
        assertEquals("45% RH", result.single().value)
    }
}