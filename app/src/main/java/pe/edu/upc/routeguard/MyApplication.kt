package pe.edu.upc.routeguard

import android.app.Application
import com.stripe.android.PaymentConfiguration
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.STRIPE_PUBLISHABLE_KEY.isNotBlank()) {
            PaymentConfiguration.init(applicationContext, BuildConfig.STRIPE_PUBLISHABLE_KEY)
        }
    }
}
