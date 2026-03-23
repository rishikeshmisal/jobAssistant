package com.jobassistant.util

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.jobassistant.domain.model.JobApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

class ExportManager @Inject constructor(
    @Suppress("UNUSED_PARAMETER") gson: Gson
) {

    // Use a dedicated Gson with UUID as string for human-readable export JSON
    private val exportGson: Gson = GsonBuilder()
        .registerTypeHierarchyAdapter(UUID::class.java, object : JsonSerializer<UUID>, JsonDeserializer<UUID> {
            override fun serialize(
                src: UUID?,
                typeOfSrc: Type?,
                context: JsonSerializationContext?
            ) = JsonPrimitive(src?.toString())

            override fun deserialize(
                json: JsonElement?,
                typeOfT: Type?,
                context: JsonDeserializationContext?
            ): UUID? = json?.asString?.let { UUID.fromString(it) }
        })
        .create()

    suspend fun exportToJson(context: Context, jobs: List<JobApplication>): Uri =
        withContext(Dispatchers.IO) {
            val dateStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
            val fileName = "jobassistant_backup_$dateStr.json"
            val dir = context.getExternalFilesDir(null)
                ?: context.filesDir          // fallback to internal storage if external unavailable
            val file = File(dir, fileName)
            file.writeText(exportGson.toJson(jobs))
            Uri.fromFile(file)
        }
}
