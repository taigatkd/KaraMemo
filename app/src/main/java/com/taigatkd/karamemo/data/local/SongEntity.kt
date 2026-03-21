package com.taigatkd.karamemo.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey val id: String,
    val artist: String,
    val title: String,
    val keyValue: Int,
    val memo: String,
    val isFavorite: Boolean,
    val playlistId: String?,
    val createdAtEpochMillis: Long,
    val scoreValue: Double?,
)
