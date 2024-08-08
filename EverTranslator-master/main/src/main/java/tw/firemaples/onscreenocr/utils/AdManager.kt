package tw.firemaples.onscreenocr.utils

import android.content.Context
import com.google.android.gms.ads.*
import tw.firemaples.onscreenocr.BuildConfig
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.log.FirebaseEvent

object AdManager {
    private val logger: Logger by lazy { Logger(AdManager::class) }

    private val context: Context get() = Utils.context
    private var initialized = false
    private val adRequestTasks = mutableListOf<() -> Unit>()
    private val testDevices: List<String>
        get() = context.resources.getStringArray(R.array.admob_test_devices).toList()

    fun init() {
        if (!BuildConfig.ENABLE_ADS) return

        MobileAds.initialize(context) {
            synchronized(AdManager) {
                initialized = true
            }

            val configuration = RequestConfiguration.Builder()
                .setTestDeviceIds(testDevices)
                .build()
            MobileAds.setRequestConfiguration(configuration)

            adRequestTasks.forEach { it.invoke() }
        }
    }

    fun loadBanner(admobAd: AdView) {
        if (!BuildConfig.ENABLE_ADS) return

        val request = AdRequest.Builder().build()
        admobAd.loadAd(request)

        val admobUnitId = admobAd.adUnitId
        admobAd.adListener = object : AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()
                logger.debug("Admob AD loaded: $admobUnitId")
            }

            override fun onAdImpression() {
                super.onAdImpression()
                logger.debug("Admob AD impression: $admobUnitId")
                FirebaseEvent.logEventAdShowSuccess(admobUnitId)
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                super.onAdFailedToLoad(error)
                logger.debug("Admob AD load failed: $admobUnitId, $error")
                FirebaseEvent.loadEventAdShowFailed(admobUnitId, error.message)
            }
        }
    }
}
