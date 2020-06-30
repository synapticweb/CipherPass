package net.synapticweb.passman.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import net.synapticweb.passman.CryptoPassApp
import net.synapticweb.passman.model.Repository
import net.synapticweb.passman.util.CPCipher
import java.io.File
import javax.inject.Inject

class SettingsViewModel @Inject constructor(private val repository: Repository,
                                            private val cipher : CPCipher,
                                            application: Application) :
    AndroidViewModel(application) {

    fun hasPasswdFile() : Boolean {
        val encFile = File(getApplication<CryptoPassApp>().filesDir.absolutePath + "/"
                + cipher.getEncryptedFilePath())
        return encFile.exists()
    }

    fun deletePasswdFile() {
        val encFile = File(getApplication<CryptoPassApp>().filesDir.absolutePath + "/"
                + cipher.getEncryptedFilePath())
        encFile.delete()
    }
}