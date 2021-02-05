package net.synapticweb.cipherpass.util

import android.content.Context

class TestCPCipherImpl(context: Context) : CPCipherImpl(context) {
    var hasHardwareStorage : Boolean = false
    var encryptPassReturnError : Boolean = false
    var decryptPassReturnError : Boolean = false

    override suspend fun isStorageHardwareBacked() : Boolean {
        return hasHardwareStorage
    }

    override fun encryptPassToSettings(passphrase: CharArray): Boolean {
        if(encryptPassReturnError)
            return false
        return super.encryptPassToSettings(passphrase)
    }

    override fun decryptPassFromSettings(): CharArray? {
        if(decryptPassReturnError)
            return null
        return super.decryptPassFromSettings()
    }
}