package com.algonetwork.routeoptimization.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TripHistoryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(tripHistory: TripHistory)

    @Delete
    fun delete(tripHistory: TripHistory)

    @Query("SELECT * FROM tripHistory ORDER BY id DESC")
    fun getAll(): Flow<List<TripHistory>>
}