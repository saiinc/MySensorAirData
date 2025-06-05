package com.saionji.mysensor.data

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class SafeDoubleDeserializer : JsonDeserializer<Double> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Double {
        return try {
            val str = json.asString
            str.toDoubleOrNull() ?: 0.0 // можно заменить на null, если nullable поле
        } catch (e: Exception) {
            0.0
        }
    }
}