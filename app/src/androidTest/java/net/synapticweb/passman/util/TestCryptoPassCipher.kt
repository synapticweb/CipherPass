package net.synapticweb.passman.util

import android.content.Context
import net.synapticweb.passman.TEST_ENCRYPTED_PASS_FILENAME
import java.io.File

class TestCryptoPassCipher(context: Context) : CryptoPassCipher(context) {
    var hasHardwareStorage : Boolean = false
    var encryptFileReturnError : Boolean = false
    var decryptFileReturnError : Boolean = false

    override fun isStorageHardwareBacked() : Boolean {
        return hasHardwareStorage
    }

    override fun getEncryptedFilePath() : String = TEST_ENCRYPTED_PASS_FILENAME

    override suspend fun encryptPassToDisk(passphrase: CharArray): Boolean {
        if(encryptFileReturnError)
            return false
        return super.encryptPassToDisk(passphrase)
    }

    override suspend fun decryptPassFromDisk(encFile: File): CharArray? {
        if(decryptFileReturnError)
            return null
        return super.decryptPassFromDisk(encFile)
    }
}