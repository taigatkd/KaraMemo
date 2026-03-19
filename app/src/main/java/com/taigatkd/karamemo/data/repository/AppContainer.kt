package com.taigatkd.karamemo.data.repository

import android.content.Context
import com.taigatkd.karamemo.data.local.KaraMemoDatabase

class AppContainer(context: Context) {
    private val database = KaraMemoDatabase.getInstance(context)

    val songRepository: SongRepository = SongRepositoryImpl(database.songDao())
    val playlistRepository: PlaylistRepository = PlaylistRepositoryImpl(database.playlistDao())
    val preferencesRepository: PreferencesRepository = PreferencesRepositoryImpl(context)
}

