package com.taigatkd.karamemo.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.taigatkd.karamemo.ads.MonetizationPolicy
import com.taigatkd.karamemo.domain.model.MAX_KARAOKE_MACHINE_SETTING
import com.taigatkd.karamemo.domain.model.KaraokeMachine
import com.taigatkd.karamemo.domain.model.KaraokeMachineSettings
import com.taigatkd.karamemo.domain.model.MIN_KARAOKE_MACHINE_SETTING
import com.taigatkd.karamemo.domain.model.SongSortType
import com.taigatkd.karamemo.domain.model.defaultMachineSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val DATASTORE_NAME = "kara_memo_prefs"
private val Context.karaMemoDataStore by preferencesDataStore(name = DATASTORE_NAME)

data class MonetizationPreferences(
    val totalSongsAdded: Int = 0,
    val pendingInterstitial: Boolean = false,
    val lastInterstitialShownAtEpochMillis: Long? = null,
    val cachedRealProEnabled: Boolean = false,
    val mockProEnabled: Boolean = false,
)

interface PreferencesRepository {
    val currentMachine: Flow<KaraokeMachine>
    val machineSettings: Flow<Map<KaraokeMachine, KaraokeMachineSettings>>
    val pinnedArtists: Flow<Set<String>>
    val pinnedPlaylists: Flow<Set<String>>
    val lastUsedArtist: Flow<String?>
    val songSortType: Flow<SongSortType>
    val monetizationPreferences: Flow<MonetizationPreferences>

    suspend fun setCurrentMachine(machine: KaraokeMachine)
    suspend fun updateMachineSettings(machine: KaraokeMachine, settings: KaraokeMachineSettings)
    suspend fun toggleArtistPin(artistName: String)
    suspend fun togglePlaylistPin(playlistId: String)
    suspend fun removeArtistPin(artistName: String)
    suspend fun removePlaylistPin(playlistId: String)
    suspend fun setLastUsedArtist(artistName: String)
    suspend fun setSongSortType(sortType: SongSortType)
    suspend fun setCachedRealProEnabled(enabled: Boolean)
    suspend fun setMockProEnabled(enabled: Boolean)
    suspend fun recordSongAdded(): MonetizationPreferences
    suspend fun recordInterstitialShown(shownAtEpochMillis: Long)
}

class PreferencesRepositoryImpl(
    private val context: Context,
) : PreferencesRepository {
    private companion object {
        const val MACHINE_SETTINGS_VERSION_LEGACY_50 = 1
        const val MACHINE_SETTINGS_VERSION_100 = 2
        const val MACHINE_SETTINGS_VERSION_CURRENT = 3
    }

    private object Keys {
        val currentMachine = stringPreferencesKey("current_machine")
        val machineSettingsVersion = intPreferencesKey("machine_settings_version")
        val pinnedArtists = stringSetPreferencesKey("pinned_artists")
        val pinnedPlaylists = stringSetPreferencesKey("pinned_playlists")
        val lastUsedArtist = stringPreferencesKey("last_used_artist")
        val songSortType = stringPreferencesKey("song_sort_type")
        val totalSongsAdded = intPreferencesKey("total_songs_added")
        val pendingInterstitial = booleanPreferencesKey("pending_interstitial")
        val lastInterstitialShownAt = longPreferencesKey("last_interstitial_shown_at")
        val cachedRealProEnabled = booleanPreferencesKey("cached_real_pro_enabled")
        val mockProEnabled = booleanPreferencesKey("mock_pro_enabled")

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
            val machineSettingsVersion = prefs[Keys.machineSettingsVersion] ?: MACHINE_SETTINGS_VERSION_LEGACY_50
            defaultMachineSettings().mapValues { (machine, defaults) ->
                KaraokeMachineSettings(
                    bgm = prefs[Keys.bgm(machine)].resolveMachineSetting(defaults.bgm, machineSettingsVersion),
                    mic = prefs[Keys.mic(machine)].resolveMachineSetting(defaults.mic, machineSettingsVersion),
                    echo = prefs[Keys.echo(machine)].resolveMachineSetting(defaults.echo, machineSettingsVersion),
                    music = prefs[Keys.music(machine)].resolveMachineSetting(defaults.music, machineSettingsVersion),
                )
            }
        }

    override val pinnedArtists: Flow<Set<String>> =
        context.karaMemoDataStore.data.map { prefs -> prefs[Keys.pinnedArtists] ?: emptySet() }

    override val pinnedPlaylists: Flow<Set<String>> =
        context.karaMemoDataStore.data.map { prefs -> prefs[Keys.pinnedPlaylists] ?: emptySet() }

    override val lastUsedArtist: Flow<String?> =
        context.karaMemoDataStore.data.map { prefs -> prefs[Keys.lastUsedArtist] }

    override val songSortType: Flow<SongSortType> =
        context.karaMemoDataStore.data.map { prefs ->
            prefs[Keys.songSortType]
                ?.let { savedValue -> runCatching { SongSortType.valueOf(savedValue) }.getOrNull() }
                ?: SongSortType.DATE_DESC
        }

    override val monetizationPreferences: Flow<MonetizationPreferences> =
        context.karaMemoDataStore.data.map { prefs ->
            MonetizationPreferences(
                totalSongsAdded = prefs[Keys.totalSongsAdded] ?: 0,
                pendingInterstitial = prefs[Keys.pendingInterstitial] ?: false,
                lastInterstitialShownAtEpochMillis = prefs[Keys.lastInterstitialShownAt],
                cachedRealProEnabled = prefs[Keys.cachedRealProEnabled] ?: false,
                mockProEnabled = prefs[Keys.mockProEnabled] ?: false,
            )
        }

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
            prefs[Keys.machineSettingsVersion] = MACHINE_SETTINGS_VERSION_CURRENT
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

    override suspend fun setSongSortType(sortType: SongSortType) {
        context.karaMemoDataStore.edit { prefs ->
            prefs[Keys.songSortType] = sortType.name
        }
    }

    override suspend fun setCachedRealProEnabled(enabled: Boolean) {
        context.karaMemoDataStore.edit { prefs ->
            prefs[Keys.cachedRealProEnabled] = enabled
        }
    }

    override suspend fun setMockProEnabled(enabled: Boolean) {
        context.karaMemoDataStore.edit { prefs ->
            prefs[Keys.mockProEnabled] = enabled
        }
    }

    override suspend fun recordSongAdded(): MonetizationPreferences {
        var updatedState = MonetizationPreferences()

        context.karaMemoDataStore.edit { prefs ->
            val nextTotal = (prefs[Keys.totalSongsAdded] ?: 0) + 1
            val shouldQueueInterstitial = nextTotal % MonetizationPolicy.SONG_ADD_MILESTONE == 0
            val pendingInterstitial = (prefs[Keys.pendingInterstitial] ?: false) || shouldQueueInterstitial

            prefs[Keys.totalSongsAdded] = nextTotal
            prefs[Keys.pendingInterstitial] = pendingInterstitial

            updatedState = MonetizationPreferences(
                totalSongsAdded = nextTotal,
                pendingInterstitial = pendingInterstitial,
                lastInterstitialShownAtEpochMillis = prefs[Keys.lastInterstitialShownAt],
                cachedRealProEnabled = prefs[Keys.cachedRealProEnabled] ?: false,
                mockProEnabled = prefs[Keys.mockProEnabled] ?: false,
            )
        }

        return updatedState
    }

    override suspend fun recordInterstitialShown(shownAtEpochMillis: Long) {
        context.karaMemoDataStore.edit { prefs ->
            prefs[Keys.pendingInterstitial] = false
            prefs[Keys.lastInterstitialShownAt] = shownAtEpochMillis
        }
    }

    private fun Int?.resolveMachineSetting(
        defaultValue: Int,
        machineSettingsVersion: Int,
    ): Int {
        val rawValue = this ?: return defaultValue
        val normalizedValue = when (machineSettingsVersion) {
            MACHINE_SETTINGS_VERSION_100 -> rawValue / 2
            else -> rawValue
        }
        return normalizedValue.coerceIn(MIN_KARAOKE_MACHINE_SETTING, MAX_KARAOKE_MACHINE_SETTING)
    }
}
