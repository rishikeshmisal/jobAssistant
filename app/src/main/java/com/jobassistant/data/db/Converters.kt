package com.jobassistant.data.db

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jobassistant.domain.model.ApplicationStatus
import javax.inject.Inject

@ProvidedTypeConverter
class Converters @Inject constructor() {

    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>): String = gson.toJson(value)

    @TypeConverter
    fun toStringList(value: String): List<String> =
        gson.fromJson(value, object : TypeToken<List<String>>() {}.type) ?: emptyList()

    @TypeConverter
    fun fromStatus(status: ApplicationStatus): String = status.name

    @TypeConverter
    fun toStatus(value: String): ApplicationStatus = ApplicationStatus.valueOf(value)
}
