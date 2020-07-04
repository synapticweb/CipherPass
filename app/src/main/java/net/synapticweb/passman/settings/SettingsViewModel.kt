package net.synapticweb.passman.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import kotlinx.coroutines.launch
import net.synapticweb.passman.APPLOCK_KEY
import net.synapticweb.passman.APPLOCK_PASSWD_VALUE
import net.synapticweb.passman.CryptoPassApp
import net.synapticweb.passman.model.Repository
import net.synapticweb.passman.util.CPCipher
import net.synapticweb.passman.util.Event
import java.io.File
import javax.inject.Inject

class SettingsViewModel @Inject constructor(private val repository: Repository,
                                            private val cipher : CPCipher,
                                            application: Application) :
    AndroidViewModel(application) {

    val changePassWorking = MutableLiveData<Boolean>()
    val changePassFinish = MutableLiveData<Event<Boolean>>()
    val changePassInvalid = MutableLiveData<Event<Boolean>>()
    val changePassNoMatch = MutableLiveData<Event<Boolean>>()

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

    private fun weakAuthentication() : Boolean {
        val settings = PreferenceManager.getDefaultSharedPreferences(getApplication())
        return settings.getString(APPLOCK_KEY, APPLOCK_PASSWD_VALUE) != APPLOCK_PASSWD_VALUE
    }

    fun changePass(actualPass : CharArray, newPass : CharArray, reNewPass : CharArray) {
        viewModelScope.launch {
            changePassWorking.value = true

            if(!newPass.contentEquals(reNewPass)) {
                changePassNoMatch.value = Event(true)
                changePassWorking.value = false
                return@launch
            }

            if(!repository.isPassValid(actualPass, true)) {
                changePassInvalid.value = Event(true)
                changePassWorking.value = false
                return@launch
            }

            repository.createPassHash(newPass)

            if(weakAuthentication() && !encryptPassToDisk(newPass, cipher, false)) {
                changePassFinish.value = Event(false)
                changePassWorking.value = false
                return@launch
            }

            changePassFinish.value = Event(repository.reKey(newPass))
            changePassWorking.value = false
        }
    }
}