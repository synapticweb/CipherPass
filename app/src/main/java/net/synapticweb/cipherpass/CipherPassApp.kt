package net.synapticweb.cipherpass

import android.app.Application
import net.synapticweb.cipherpass.di.AppComponent
import net.synapticweb.cipherpass.di.DaggerAppComponent

open class CipherPassApp : Application() {
    val appComponent : AppComponent by lazy {
       initializeComponent()
    }

    open fun initializeComponent() : AppComponent {
        return DaggerAppComponent.factory().create(applicationContext, this)
    }
}