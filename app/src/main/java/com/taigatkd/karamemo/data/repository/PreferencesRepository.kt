package com.taigatkd.karamemo.data.repository

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.taigatkd.karamemo.domain.model.KaraokeMachine
import com.taigatkd.karamemo.domain.model.KaraokeMachineSettings
import com.taigatkd.karamemo.domain.model.defaultMachineSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val DATASTORE_NAME = "kara_memo_prefs"
private val Context.karaMemoDataStore by preferencesDataStore(name = DATASTORE_NAME)

interface PreferencesRepository {
    val currentMachine: Flow<KaraokeMachine>
    val machineSettings: Flow<Map<KaraokeMachine, KaraokeMachineSettings>>
    val pinnedArtists: Flow<Set<String>>
    val pinnedPlaylists: Flow<Set<String>>
    val lastUsedArtist: Flow<String?>

    suspend fun setCurrentMachine(machine: KaraokeMachine)
    suspend fun updateMachineSettings(machine: KaraokeMachine, settings: KaraokeMachineSettings)
    suspend fun toggleArtistPin(artistName: String)
    suspend fun togglePlaylistPin(playlistId: String)
    suspend fun removeArtistPin(artistName: String)
    suspend fun removePlaylistPin(playlistId: String)
    suspend fun setLastUsedArtist(artistName: String)
}

class PreferencesRepositoryImpl(
    private val context: Context,
) : PreferencesRepository {
    private object Keys {
        val currentMachine = stringPreferencesKey("current_machine")
        val pinnedArtists = stringSetPreferencesKey("pinned_artists")
        val pinnedPlaylists = stringSetPreferencesKey("pinned_playlists")
        val lastUsedArtist = stringPreferencesKey("last_used_artist")

        fun bgm(machine: KaraokeMachine): Preferences.Key<Int> = intPreferencesKey("${machine.name.lowercase()}_bgm")
        fun mic(machine: KaraokeMachine): Preferences.Key<Int> = intPreferencesKey("${machine.name.lowercase()}_mic")
        fun echo(machine: KaraokeMachine): Preferences.Key<Int> = intPreferencesKey("${machine.name.lowercase()}_echo")
        fun music(machine: KaraokeMachine): Preferences.Key<Int> = intPreferencesKey("${machine.name.lowercase()}_music")
    }

    override val currentMachine: Flow<KaraokeMachine> =
        context.karaMemoDataStore.data.map { prefs ->
            prefs[Keys.currentMachine]
                ?.let { value -> runCatching { KaraokeMachine.valueOf(value) }.getOrNull() }
                ?: KaraokeMachine.DAM
        }

    override val machineSettings: Flow<Map<KaraokeMachine, KaraokeMachineSettings>> =
        context.karaMemoDataStore.data.map { prefs ->
            defaultMachineSettings().mapValues { (machine, defaults) ->
                KaraokeMachineSettings(
                    bgm = prefs[Keys.bgm(machine)] ?: defaults.bgm,
                    mic = prefs[Keys.mic(machine)] ?: defaults.mic,
                    echo = prefs[Keys.echo(machine)] ?: defaults.echo,
                    music = prefs[Keys.music(machine)] ?: defaults.music,
                )
            }
        }

    override val pinnedArtists: Flow<Set<String>> =
        context.karaMemoDataStore.data.map { prefs -> prefs[Keys.pinnedArtists] ?: emptySet() }

    override val pinnedPlaylists: Flow<Set<String>> =
        context.karaMemoDataStore.data.map { prefs -> prefs[Keys.pinnedPlaylists] ?: emptySet() }

    override val lastUsedArtist: Flow<String?> =
        context.karaMemoDataStore.data.map { prefs -> prefs[Keys.lastUsedArtist] }

    override suspend fun setCurrentMachine(machine: KaraokeMachine) {
        context.karaMemoDataStore.edit { prefs ->
            prefs[Keys.currentMachine] = machine.name
        }
    }

    override suspend fun updateMachineSettings(machine: KaraokeMachine, settings: KaraokeMachineSettings) {
        context.karaMemoDataStore.edit { prefs ->
            prefs[Keys.bgm(machine)] = settings.bgm
            prefs[Keys.mic(machine)] = settings.mic
            prefs[Keys.echo(machine)] = settings.echo
            prefs[Keys.music(machine)] = settings.music
        }
    }

    override suspend fun toggleArtistPin(artistName: String) {
        context.karaMemoDataStore.edit { prefs ->
            val current = prefs[Keys.pinnedArtists]?.toMutableSet() ?: mutableSetOf()
            if (!current.add(artistName)) {
                current.remove(artistName)
            }
            prefs[Keys.pinnedArtists] = current
        }
    }

    override suspend fun togglePlaylistPin(playlistId: String) {
        context.karaMemoDataStore.edit { prefs ->
            val current = prefs[Keys.pinnedPlaylists]?.toMutableSet() ?: mutableSetOf()
            if (!current.add(playlistId)) {
                current.remove(playlistId)
            }
            prefs[Keys.pinnedPlaylists] = current
        }
    }

    override suspend fun removeArtistPin(artistName: String) {
        context.karaMemoDataStore.edit { prefs ->
            val current = prefs[Keys.pinnedArtists]?.toMutableSet() ?: mutableSetOf()
            current.remove(artistName)
            prefs[Keys.pinnedArtists] = current
        }
    }

    override suspend fun removePlaylistPin(playlistId: String) {
        context.karaMemoDataStore.edit { prefs ->
            val current = prefs[Keys.pinnedPlaylists]?.toMutableSet() ?: mutableSetOf()
            current.remove(playlistId)
            prefs[Keys.pinnedPlaylists] = current
        }
    }

    override suspend fun setLastUsedArtist(artistName: String) {
        context.karaMemoDataStore.edit { prefs ->
            prefs[Keys.lastUsedArtist] = artistName
        }
    }
}
