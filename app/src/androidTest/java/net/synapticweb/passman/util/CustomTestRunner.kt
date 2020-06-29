package net.synapticweb.passman.util

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import net.synapticweb.passman.TestCryptoPassApp

class CustomTestRunner : AndroidJUnitRunner() {

    override fun newApplication(cl: ClassLoader?, name: String?, context: Context?): Application {
        return super.newApplication(cl, TestCryptoPassApp::class.java.name, context)
    }
}