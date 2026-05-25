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
    suspend fun insert(appliance: Appliance): Long  // Returns auto-generated Room ID for Firestore sync

    @Update
    suspend fun update(appliance: Appliance)

    @Query("DELETE FROM appliances WHERE id = :id")
    suspend fun deleteById(id: Int)

    // One-shot count check — used to seed defaults only on true first launch,
    // not every time the list becomes empty (which would re-seed after deletes)
    @Query("SELECT COUNT(*) FROM appliances")
    suspend fun getCount(): Int
}