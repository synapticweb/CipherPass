package net.synapticweb.passman

import android.app.Application
import net.synapticweb.passman.di.AppComponent
import net.synapticweb.passman.di.DaggerAppComponent

open class PmApp : Application() {
    val appComponent : AppComponent by lazy {
        DaggerAppComponent.factory().create(applicationContext, this)
    }
}