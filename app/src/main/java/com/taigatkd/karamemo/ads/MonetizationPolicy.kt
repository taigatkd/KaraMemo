package com.taigatkd.karamemo.ads

import com.taigatkd.karamemo.data.repository.MonetizationPreferences

object MonetizationPolicy {
    const val SONG_ADD_MILESTONE = 10
    const val INTERSTITIAL_COOLDOWN_MILLIS = 2 * 60 * 1000L
    const val MAX_INTERSTITIALS_PER_SESSION = 3

    fun canRequestInterstitial(
        preferences: MonetizationPreferences,
        sessionInterstitialShownCount: Int,
        nowEpochMillis: Long,
    ): Boolean {
        if (!preferences.pendingInterstitial) return false
        if (sessionInterstitialShownCount >= MAX_INTERSTITIALS_PER_SESSION) return false

        val lastShownAt = preferences.lastInterstitialShownAtEpochMillis ?: return true
        return nowEpochMillis - lastShownAt >= INTERSTITIAL_COOLDOWN_MILLIS
    }
}

enum class NaturalBreakPoint {
    SONG_EDITOR_DISMISSED,
    PLAYLIST_PICKER_DISMISSED,
    TAB_SWITCHED,
}

data class InterstitialOpportunity(
    val breakPoint: NaturalBreakPoint,
    val totalSongsAdded: Int,
)
