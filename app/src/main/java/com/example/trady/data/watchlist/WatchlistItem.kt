package com.example.trady.data.watchlist

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.*

@Entity(
    tableName = "watchlist_items",
    foreignKeys = [
        ForeignKey(
            entity = Watchlist::class,
            parentColumns = ["id"],
            childColumns  = ["watchlistId"],
            onDelete      = ForeignKey.CASCADE
        )
    ],
    indices = [Index("watchlistId")]
)
data class WatchlistItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val watchlistId: Long,
    val symbol:      String
)