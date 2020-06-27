package net.synapticweb.passman

import net.synapticweb.passman.di.AppComponent
import net.synapticweb.passman.di.DaggerTestAppComponent

class TestCryptoPassApp : CryptoPassApp() {
    override fun initializeComponent(): AppComponent {
        return DaggerTestAppComponent.factory().create(applicationContext, this)
    }

}