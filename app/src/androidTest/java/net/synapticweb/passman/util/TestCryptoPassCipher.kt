package net.synapticweb.passman.util

import android.content.Context
import net.synapticweb.passman.TEST_ENCRYPTED_PASS_FILENAME

class TestCryptoPassCipher(context: Context) : CryptoPassCipher(context) {
    var hasHardwareStorage : Boolean = false

    override fun isStorageHardwareBacked() : Boolean {
        return hasHardwareStorage
    }

    override fun getEncryptedFilePath() : String = TEST_ENCRYPTED_PASS_FILENAME
}