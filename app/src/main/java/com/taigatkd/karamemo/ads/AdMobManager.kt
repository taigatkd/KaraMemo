package com.taigatkd.karamemo.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.taigatkd.karamemo.BuildConfig

class AdMobManager(
    private val context: Context,
) {
    private var interstitialAd: InterstitialAd? = null
    private var initialized = false
    private var loadingInterstitial = false

    fun initialize() {
        if (!BuildConfig.ADS_ENABLED || initialized) return
        MobileAds.initialize(context) {}
        initialized = true
    }

    fun preloadInterstitial() {
        if (
            !BuildConfig.ADS_ENABLED ||
            BuildConfig.INTERSTITIAL_AD_UNIT_ID.isBlank() ||
            loadingInterstitial ||
            interstitialAd != null
        ) {
            return
        }

        loadingInterstitial = true
        InterstitialAd.load(
            context,
            BuildConfig.INTERSTITIAL_AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    loadingInterstitial = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    loadingInterstitial = false
                }
            },
        )
    }

    fun showInterstitialIfAvailable(
        activity: Activity,
        onShown: () -> Unit,
        onUnavailable: () -> Unit,
    ): Boolean {
        val ad = interstitialAd
        if (ad == null) {
            preloadInterstitial()
            return false
        }

        interstitialAd = null
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                onShown()
            }

            override fun onAdDismissedFullScreenContent() {
                preloadInterstitial()
            }

            override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                preloadInterstitial()
                onUnavailable()
            }
        }
        ad.show(activity)
        return true
    }
}
