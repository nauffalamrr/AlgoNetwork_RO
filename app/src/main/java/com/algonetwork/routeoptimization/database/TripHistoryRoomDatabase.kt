package com.algonetwork.routeoptimization.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TripHistory::class], version = 1)
abstract class TripHistoryRoomDatabase : RoomDatabase() {
    abstract fun tripHistoryDao(): TripHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: TripHistoryRoomDatabase? = null

        @JvmStatic
        fun getDatabase(context: Context): TripHistoryRoomDatabase {
            if (INSTANCE == null) {
                synchronized(TripHistoryRoomDatabase::class.java) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                        TripHistoryRoomDatabase::class.java, "trip_history_database")
                        .build()
                }
            }
            return INSTANCE as TripHistoryRoomDatabase
        }
    }
}