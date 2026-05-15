package com.fit5046.wattwise

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ApplianceDao {

    @Query("SELECT * FROM appliances ORDER BY name ASC")
    fun getAll(): Flow<List<Appliance>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(appliance: Appliance)

    @Update
    suspend fun update(appliance: Appliance)

    @Query("DELETE FROM appliances WHERE id = :id")
    suspend fun deleteById(id: Int)
}

