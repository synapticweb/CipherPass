package net.synapticweb.cipherpass

import android.app.Application
import android.content.Context
import net.synapticweb.cipherpass.di.AppComponent
import net.synapticweb.cipherpass.di.DaggerAppComponent
import org.acra.ACRA
import org.acra.annotation.AcraCore
import org.acra.annotation.AcraHttpSender
import org.acra.data.StringFormat
import org.acra.sender.HttpSender
import org.matomo.sdk.Matomo
import org.matomo.sdk.Tracker
import org.matomo.sdk.TrackerBuilder

const val APP_TAG = "CipherPass"

@AcraCore(reportFormat = StringFormat.KEY_VALUE_LIST)
@AcraHttpSender(
    uri = "http://crashes.synapticweb.net",
    httpMethod = HttpSender.Method.POST
)
open class CipherPassApp : Application() {
    val appComponent : AppComponent by lazy {
       initializeComponent()
    }
    private var tracker : Tracker? = null

    open fun initializeComponent() : AppComponent {
        return DaggerAppComponent.factory().create(applicationContext, this)
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        if (!BuildConfig.DEBUG)
            ACRA.init(this)
    }

    @Synchronized
    fun getTracker() : Tracker {
        if(tracker == null)
            tracker = TrackerBuilder.createDefault("https://matomo.synapticweb.net/matomo.php", 1).build(Matomo.getInstance(this))
        return tracker as Tracker
    }
}