package net.synapticweb.cipherpass

import android.app.Application
import net.synapticweb.cipherpass.di.AppComponent
import net.synapticweb.cipherpass.di.DaggerAppComponent

const val APP_TAG = "CipherPass"

open class CipherPassApp : Application() {
    val appComponent : AppComponent by lazy {
       initializeComponent()
    }

    open fun initializeComponent() : AppComponent {
        return DaggerAppComponent.factory().create(applicationContext, this)
    }
}