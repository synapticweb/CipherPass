package net.synapticweb.cipherpass

import net.synapticweb.cipherpass.di.AppComponent
import net.synapticweb.cipherpass.di.DaggerTestAppComponent

class TestCipherPassApp : CipherPassApp() {
    override fun initializeComponent(): AppComponent {
        return DaggerTestAppComponent.factory().create(applicationContext, this)
    }

}