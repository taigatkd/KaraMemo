package com.taigatkd.karamemo.domain.model

import java.time.Instant

data class Playlist(
    val id: String,
    val name: String,
    val createdAt: Instant,
)

