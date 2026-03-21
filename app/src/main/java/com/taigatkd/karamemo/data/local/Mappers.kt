package com.taigatkd.karamemo.data.local

import com.taigatkd.karamemo.domain.model.Playlist
import com.taigatkd.karamemo.domain.model.Song
import java.time.Instant

fun SongEntity.toDomain(): Song =
    Song(
        id = id,
        artist = artist,
        title = title,
        key = keyValue,
        memo = memo,
        isFavorite = isFavorite,
        playlistId = playlistId,
        createdAt = Instant.ofEpochMilli(createdAtEpochMillis),
        score = scoreValue,
    )

fun Song.toEntity(): SongEntity =
    SongEntity(
        id = id,
        artist = artist,
        title = title,
        keyValue = key,
        memo = memo,
        isFavorite = isFavorite,
        playlistId = playlistId,
        createdAtEpochMillis = createdAt.toEpochMilli(),
        scoreValue = score,
    )

fun PlaylistEntity.toDomain(): Playlist =
    Playlist(
        id = id,
        name = name,
        createdAt = Instant.ofEpochMilli(createdAtEpochMillis),
    )

fun Playlist.toEntity(): PlaylistEntity =
    PlaylistEntity(
        id = id,
        name = name,
        createdAtEpochMillis = createdAt.toEpochMilli(),
    )
