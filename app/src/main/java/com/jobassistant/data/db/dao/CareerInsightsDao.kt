package com.jobassistant.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.jobassistant.data.db.entity.CareerInsightsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CareerInsightsDao {

    @Query("SELECT * FROM career_insights ORDER BY generatedDate DESC LIMIT 1")
    fun getLatestAsFlow(): Flow<CareerInsightsEntity?>

    @Upsert
    suspend fun upsert(entity: CareerInsightsEntity)
}
