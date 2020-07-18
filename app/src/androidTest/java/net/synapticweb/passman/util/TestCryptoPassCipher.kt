package net.synapticweb.passman.util

import android.content.Context

class TestCryptoPassCipher(context: Context) : CryptoPassCipher(context) {
    var hasHardwareStorage : Boolean = false
    var encryptPassReturnError : Boolean = false
    var decryptPassReturnError : Boolean = false

    override fun isStorageHardwareBacked() : Boolean {
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