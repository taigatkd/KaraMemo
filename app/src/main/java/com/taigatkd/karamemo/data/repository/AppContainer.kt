package com.taigatkd.karamemo.data.repository

import android.content.Context
import com.taigatkd.karamemo.ads.AdMobManager
import com.taigatkd.karamemo.billing.BillingRepository
import com.taigatkd.karamemo.billing.BillingRepositoryImpl
import com.taigatkd.karamemo.data.local.KaraMemoDatabase

class AppContainer(context: Context) {
    private val database = KaraMemoDatabase.getInstance(context)

    val songRepository: SongRepository = SongRepositoryImpl(database.songDao())
    val playlistRepository: PlaylistRepository = PlaylistRepositoryImpl(database.playlistDao())
    val preferencesRepository: PreferencesRepository = PreferencesRepositoryImpl(context)
    val billingRepository: BillingRepository = BillingRepositoryImpl(
        context = context,
        preferencesRepository = preferencesRepository,
    )
    val adMobManager = AdMobManager(context.applicationContext)
}
