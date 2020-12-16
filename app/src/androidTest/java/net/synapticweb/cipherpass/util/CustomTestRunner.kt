package net.synapticweb.cipherpass.util

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import net.synapticweb.cipherpass.TestCipherPassApp

class CustomTestRunner : AndroidJUnitRunner() {

    override fun newApplication(cl: ClassLoader?, name: String?, context: Context?): Application {
        return super.newApplication(cl, TestCipherPassApp::class.java.name, context)
    }
}