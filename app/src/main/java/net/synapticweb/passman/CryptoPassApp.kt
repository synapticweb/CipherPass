package net.synapticweb.passman

import android.app.Application
import net.synapticweb.passman.di.AppComponent
import net.synapticweb.passman.di.DaggerAppComponent

open class CryptoPassApp : Application() {
    val appComponent : AppComponent by lazy {
       initializeComponent()
    }

    open fun initializeComponent() : AppComponent {
        return DaggerAppComponent.factory().create(applicationContext, this)
    }
}