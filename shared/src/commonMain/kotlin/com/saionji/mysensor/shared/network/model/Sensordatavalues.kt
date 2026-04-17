package com.saionji.mysensor.shared.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive

@Serializable
data class Sensordatavalues(
    @SerialName("id")
    val id: Long? = null,

    @SerialName("value_type")
    var valueType: String? = null,

    @SerialName("value")
    @Serializable(with = NullableFlexibleDoubleSerializer::class)
    var value: Double? = null
)

@Serializer(forClass = Double::class)
object NullableFlexibleDoubleSerializer : KSerializer<Double?> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("NullableFlexibleDouble", PrimitiveKind.DOUBLE)

    override fun deserialize(decoder: Decoder): Double? {
        val jsonDecoder = decoder as? JsonDecoder ?: return runCatching {
            decoder.decodeDouble()
        }.getOrNull()

        val element = jsonDecoder.decodeJsonElement()
        if (element is JsonNull) return null

        val primitive = element as? JsonPrimitive ?: return null
        val content = primitive.content.trim()
        if (content.isEmpty()) return null
        if (content.equals("nan", ignoreCase = true)) return null
        if (content.equals("null", ignoreCase = true)) return null

        return content.toDoubleOrNull()
    }

    override fun serialize(encoder: Encoder, value: Double?) {
        if (value == null) {
            encoder.encodeNull()
        } else {
            encoder.encodeDouble(value)
        }
    }
}
