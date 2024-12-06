package com.algonetwork.routeoptimization.database

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity
@Parcelize
data class TripHistory (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int = 0,

    @ColumnInfo(name = "date")
    var date: String? = null,

    @ColumnInfo(name = "status")
    var status: String? = null,

    @ColumnInfo(name = "from")
    var from: String? = null,

    @ColumnInfo(name = "destination1")
    var destination1: String? = null,

    @ColumnInfo(name = "destination2")
    var destination2: String? = null,

    @ColumnInfo(name = "destination3")
    var destination3: String? = null,

    @ColumnInfo(name = "vehicle")
    var vehicle: Int
) : Parcelable