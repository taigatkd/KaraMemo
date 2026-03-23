package com.taigatkd.karamemo.debug

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.taigatkd.karamemo.data.local.KaraMemoDatabase
import com.taigatkd.karamemo.data.local.PlaylistEntity
import com.taigatkd.karamemo.data.local.SongEntity
import com.taigatkd.karamemo.data.repository.PreferencesRepositoryImpl
import com.taigatkd.karamemo.domain.model.KaraokeMachine
import com.taigatkd.karamemo.domain.model.KaraokeMachineSettings
import com.taigatkd.karamemo.domain.model.SongSortType
import com.taigatkd.karamemo.ui.app.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MarketingSeedActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            seedMarketingData()
            startActivity(
                Intent(this@MarketingSeedActivity, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                },
            )
            finish()
        }
    }

    private suspend fun seedMarketingData() {
        withContext(Dispatchers.IO) {
            val db = KaraMemoDatabase.getInstance(applicationContext)
            db.clearAllTables()

            val playlists = listOf(
                PlaylistEntity(
                    id = "playlist-favorites",
                    name = "Favorites",
                    createdAtEpochMillis = 1_710_000_000_000,
                ),
                PlaylistEntity(
                    id = "playlist-practice",
                    name = "Practice Set",
                    createdAtEpochMillis = 1_710_000_001_000,
                ),
                PlaylistEntity(
                    id = "playlist-highnotes",
                    name = "High Notes",
                    createdAtEpochMillis = 1_710_000_002_000,
                ),
            )
            playlists.forEach { playlist ->
                db.playlistDao().upsert(playlist)
            }

            val songs = listOf(
                SongEntity(
                    id = "song-01",
                    artist = "Aster",
                    title = "Skyline",
                    keyValue = 2,
                    memo = "Save breath for the final chorus.",
                    isFavorite = true,
                    playlistId = "playlist-favorites",
                    createdAtEpochMillis = 1_710_000_003_000,
                    scoreValue = 96.2,
                ),
                SongEntity(
                    id = "song-02",
                    artist = "Nova June",
                    title = "Paper Moon",
                    keyValue = -1,
                    memo = "Keep the first verse relaxed.",
                    isFavorite = false,
                    playlistId = "playlist-practice",
                    createdAtEpochMillis = 1_710_000_004_000,
                    scoreValue = 91.5,
                ),
                SongEntity(
                    id = "song-03",
                    artist = "City Echo",
                    title = "Signal",
                    keyValue = 0,
                    memo = "Focus on the fast phrasing.",
                    isFavorite = false,
                    playlistId = "playlist-highnotes",
                    createdAtEpochMillis = 1_710_000_005_000,
                    scoreValue = 93.8,
                ),
                SongEntity(
                    id = "song-04",
                    artist = "Blue Harbor",
                    title = "Afterglow",
                    keyValue = -2,
                    memo = "Lean into the long notes.",
                    isFavorite = true,
                    playlistId = "playlist-favorites",
                    createdAtEpochMillis = 1_710_000_006_000,
                    scoreValue = 94.6,
                ),
                SongEntity(
                    id = "song-05",
                    artist = "Lumen Park",
                    title = "Daybreak",
                    keyValue = 1,
                    memo = "Keep the verse soft and steady.",
                    isFavorite = false,
                    playlistId = "playlist-practice",
                    createdAtEpochMillis = 1_710_000_007_000,
                    scoreValue = 92.4,
                ),
                SongEntity(
                    id = "song-06",
                    artist = "Mellow Tide",
                    title = "First Snow",
                    keyValue = -3,
                    memo = "Open the vowels on the hook.",
                    isFavorite = true,
                    playlistId = null,
                    createdAtEpochMillis = 1_710_000_008_000,
                    scoreValue = 95.1,
                ),
                SongEntity(
                    id = "song-07",
                    artist = "North Harbor",
                    title = "Silver Line",
                    keyValue = 0,
                    memo = "Stay light on the rhythm.",
                    isFavorite = false,
                    playlistId = "playlist-highnotes",
                    createdAtEpochMillis = 1_710_000_009_000,
                    scoreValue = 90.9,
                ),
                SongEntity(
                    id = "song-08",
                    artist = "Backlight",
                    title = "Horizon",
                    keyValue = 1,
                    memo = "Build power toward the last hook.",
                    isFavorite = false,
                    playlistId = null,
                    createdAtEpochMillis = 1_710_000_010_000,
                    scoreValue = 89.7,
                ),
            )
            songs.forEach { song ->
                db.songDao().upsert(song)
            }

            applicationContext.filesDir
                .resolve("datastore")
                .resolve("kara_memo_prefs.preferences_pb")
                .delete()

            val preferences = PreferencesRepositoryImpl(applicationContext)
            preferences.setCurrentMachine(KaraokeMachine.DAM)
            preferences.updateMachineSettings(
                machine = KaraokeMachine.DAM,
                settings = KaraokeMachineSettings(
                    bgm = 20,
                    mic = 28,
                    echo = 22,
                    music = 26,
                ),
            )
            preferences.updateMachineSettings(
                machine = KaraokeMachine.JOYSOUND,
                settings = KaraokeMachineSettings(
                    bgm = 18,
                    mic = 30,
                    echo = 25,
                    music = 24,
                ),
            )
            preferences.toggleArtistPin("Aster")
            preferences.togglePlaylistPin("playlist-favorites")
            preferences.setSongSortType(SongSortType.DATE_DESC)
            preferences.setCachedRealProEnabled(false)
            preferences.setMockProEnabled(true)
        }
    }
}
