package com.example.data.ads

import android.app.Activity
import android.content.Context
import android.os.SystemClock
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.BuildConfig
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object AdMobManager {
    private const val TAG = "AdMobManager"
    private const val MIN_INTERSTITIAL_INTERVAL_MS = 180_000L // 3 minutes cooldown

    private var isInitialized = false
    private var interstitialAd: InterstitialAd? = null
    private var isInterstitialLoading = false
    private var lastInterstitialShownTimestamp: Long = 0L

    fun initialize(context: Context) {
        if (isInitialized) return
        try {
            MobileAds.initialize(context) {
                isInitialized = true
                Log.d(TAG, "AdMob MobileAds initialized")
            }
        } catch (e: Exception) {
            Log.e(TAG, "AdMob initialization failed gracefully: ${e.message}")
        }
    }

    fun getBannerAdUnitId(): String {
        return BuildConfig.ADMOB_BANNER_ID
    }

    fun getInterstitialAdUnitId(): String {
        return BuildConfig.ADMOB_INTERSTITIAL_ID
    }

    fun preloadInterstitial(context: Context) {
        if (interstitialAd != null || isInterstitialLoading) return
        isInterstitialLoading = true

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            getInterstitialAdUnitId(),
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isInterstitialLoading = false
                    Log.d(TAG, "Interstitial ad preloaded successfully")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    interstitialAd = null
                    isInterstitialLoading = false
                    Log.w(TAG, "Interstitial ad failed to preload: ${loadAdError.message}")
                }
            }
        )
    }

    fun showInterstitialIfEligible(
        activity: Activity,
        onAdDismissed: () -> Unit
    ) {
        val now = SystemClock.elapsedRealtime()
        val elapsedSinceLast = now - lastInterstitialShownTimestamp

        if (elapsedSinceLast < MIN_INTERSTITIAL_INTERVAL_MS) {
            Log.d(TAG, "Interstitial skipped due to frequency capping ($elapsedSinceLast ms < $MIN_INTERSTITIAL_INTERVAL_MS ms)")
            onAdDismissed()
            return
        }

        val ad = interstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    lastInterstitialShownTimestamp = SystemClock.elapsedRealtime()
                    preloadInterstitial(activity)
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    interstitialAd = null
                    preloadInterstitial(activity)
                    onAdDismissed()
                }

                override fun onAdShowedFullScreenContent() {
                    lastInterstitialShownTimestamp = SystemClock.elapsedRealtime()
                }
            }
            ad.show(activity)
        } else {
            // Ad not ready: do not block user, proceed immediately and preload
            preloadInterstitial(activity)
            onAdDismissed()
        }
    }
}

@Composable
fun AdBannerView(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { context ->
                AdView(context).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = AdMobManager.getBannerAdUnitId()
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
}
