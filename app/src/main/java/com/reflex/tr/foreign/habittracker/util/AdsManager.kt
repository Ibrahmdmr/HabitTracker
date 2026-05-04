package com.reflex.tr.foreign.habittracker.util

import android.app.Activity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class AdsManager(
    private val activity: Activity
) {
    private var interstitialAd: InterstitialAd? = null
    private var importantActionCount = 0
    private var isLoadingInterstitial = false

    fun initialize() {
        MobileAds.initialize(activity)
        preloadInterstitial()
    }

    fun recordImportantAction() {
        importantActionCount++
        if (importantActionCount < ACTIONS_BETWEEN_INTERSTITIAL) {
            preloadInterstitial()
            return
        }

        val ad = interstitialAd
        if (ad == null || !activity.isUsable()) {
            preloadInterstitial()
            return
        }

        importantActionCount = 0
        interstitialAd = null
        ad.show(activity)
    }

    private fun preloadInterstitial() {
        if (isLoadingInterstitial || interstitialAd != null || !activity.isUsable()) return

        isLoadingInterstitial = true
        InterstitialAd.load(
            activity,
            AdMobIds.Interstitial,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    isLoadingInterstitial = false
                    interstitialAd = ad.apply {
                        fullScreenContentCallback = object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                preloadInterstitial()
                            }

                            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                preloadInterstitial()
                            }
                        }
                    }
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    isLoadingInterstitial = false
                    interstitialAd = null
                }
            }
        )
    }

    companion object {
        private const val ACTIONS_BETWEEN_INTERSTITIAL = 4
    }
}

private fun Activity.isUsable(): Boolean = !isFinishing && !isDestroyed
