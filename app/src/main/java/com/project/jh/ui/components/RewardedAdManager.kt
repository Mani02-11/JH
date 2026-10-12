package com.project.jh.ui.components

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object RewardedAdManager {

    private const val TAG = "RewardedAdManager"
    private var rewardedAd: RewardedAd? = null
    private var isLoading = false

    // Live Rewarded Ad Unit ID: ca-app-pub-5216504950974851/4400344156
    private const val AD_UNIT_ID = "ca-app-pub-5216504950974851/4400344156"

    fun loadAd(context: Context, onLoaded: (() -> Unit)? = null) {
        if (isLoading || rewardedAd != null) {
            if (rewardedAd != null) {
                onLoaded?.invoke()
            }
            return
        }

        isLoading = true
        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(
            context,
            AD_UNIT_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isLoading = false
                    Log.d(TAG, "Rewarded Ad loaded successfully.")
                    onLoaded?.invoke()
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    isLoading = false
                    Log.e(TAG, "Rewarded Ad failed to load: code ${error.code}, message ${error.message}")
                }
            }
        )
    }

    fun showAd(activity: Activity, onRewardEarned: (RewardItem) -> Unit, onAdClosed: (() -> Unit)? = null) {
        val ad = rewardedAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    onAdClosed?.invoke()
                    // Preload next ad
                    loadAd(activity)
                }

                override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                    rewardedAd = null
                    onAdClosed?.invoke()
                    loadAd(activity)
                }
            }

            ad.show(activity) { rewardItem ->
                Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                onRewardEarned(rewardItem)
            }
        } else {
            Log.w(TAG, "Rewarded Ad not ready yet. Preloading...")
            loadAd(activity)
        }
    }

    fun isAdLoaded(): Boolean {
        return rewardedAd != null
    }
}
