package com.taigatkd.karamemo.domain.model

import java.time.Instant

data class Song(
    val id: String,
    val artist: String,
    val title: String,
    val key: Int = 0,
    val memo: String = "",
    val isFavorite: Boolean = false,
    val playlistId: String? = null,
    val createdAt: Instant,
)

