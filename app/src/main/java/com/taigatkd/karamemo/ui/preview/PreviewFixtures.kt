package com.taigatkd.karamemo.ui.preview

import com.taigatkd.karamemo.domain.model.KaraokeMachine
import com.taigatkd.karamemo.domain.model.KaraokeMachineSettings
import com.taigatkd.karamemo.domain.model.Playlist
import com.taigatkd.karamemo.domain.model.Song
import com.taigatkd.karamemo.domain.model.defaultMachineSettings
import java.time.Instant

internal object PreviewFixtures {
    val playlists: List<Playlist> = listOf(
        Playlist(
            id = "playlist-favorites",
            name = "Favorites",
            createdAt = Instant.ofEpochMilli(1_000),
        ),
        Playlist(
            id = "playlist-practice",
            name = "Practice Set",
            createdAt = Instant.ofEpochMilli(2_000),
        ),
    )

    val playlistNamesById: Map<String, String> =
        playlists.associate { playlist -> playlist.id to playlist.name }

    val songs: List<Song> = listOf(
        Song(
            id = "song-1",
            artist = "Aimer",
            title = "Ref:rain",
            key = 2,
            memo = "Watch the breath before the chorus.",
            isFavorite = true,
            playlistId = "playlist-favorites",
            createdAt = Instant.ofEpochMilli(3_000),
            score = 96.2,
        ),
        Song(
            id = "song-2",
            artist = "Ado",
            title = "Show",
            key = -1,
            memo = "Keep the first verse relaxed.",
            isFavorite = false,
            playlistId = "playlist-practice",
            createdAt = Instant.ofEpochMilli(2_000),
            score = 91.5,
        ),
        Song(
            id = "song-3",
            artist = "YOASOBI",
            title = "Idol",
            key = 0,
            memo = "",
            isFavorite = false,
            playlistId = null,
            createdAt = Instant.ofEpochMilli(1_000),
            score = null,
        ),
    )

    val pinnedArtists: Set<String> = setOf("Aimer")
    val pinnedPlaylists: Set<String> = setOf("playlist-favorites")

    val machineSettings: Map<KaraokeMachine, KaraokeMachineSettings> =
        defaultMachineSettings() + mapOf(
            KaraokeMachine.DAM to KaraokeMachineSettings(
                bgm = 20,
                mic = 28,
                echo = 22,
                music = 26,
            ),
            KaraokeMachine.JOYSOUND to KaraokeMachineSettings(
                bgm = 18,
                mic = 30,
                echo = 25,
                music = 24,
            ),
        )
}
