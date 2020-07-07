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
import net.synapticweb.passman.HASH_TYPE_KEY
import net.synapticweb.passman.model.Repository
import net.synapticweb.passman.util.CPCipher
import net.synapticweb.passman.util.Event
import net.synapticweb.passman.util.wrapEspressoIdlingResource
import java.io.File
import java.util.*
import javax.inject.Inject

class SettingsViewModel @Inject constructor(private val repository: Repository,
                                            private val cipher : CPCipher,
                                            application: Application) :
    AndroidViewModel(application) {

    val passWorking = MutableLiveData<Boolean>()
    val passFinish = MutableLiveData<Event<Boolean>>()
    val passInvalid = MutableLiveData<Event<Boolean>>()
    val passNoMatch = MutableLiveData<Event<Boolean>>()

    fun setPref(newValue : String) {
        val settings = PreferenceManager.getDefaultSharedPreferences(getApplication())
        val editor = settings.edit()
        editor.putString(HASH_TYPE_KEY, newValue)
        editor.apply()
        editor.commit()
    }

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
            passWorking.value = true
            if(!newPass.contentEquals(reNewPass)) {
                passNoMatch.value = Event(true)
                passWorking.value = false
                return@launch
            }

            wrapEspressoIdlingResource {
               if(!repository.isPassValid(actualPass, true)) {
                   passInvalid.value = Event(true)
                   passWorking.value = false
                   return@launch
               }
                if(!repository.createPassHash(newPass, null)) {
                    passFinish.value = Event(false)
                    passWorking.value = false
                    return@launch
                }
            }

            wrapEspressoIdlingResource {
                if (weakAuthentication() && !encryptPassToDisk(newPass, cipher)) {
                    passFinish.value = Event(false)
                    passWorking.value = false
                    return@launch
                }
            }

            passFinish.value = Event(repository.reKey(newPass))
            passWorking.value = false
            Arrays.fill(actualPass, 0.toChar())
            Arrays.fill(newPass, 0.toChar())
            Arrays.fill(reNewPass, 0.toChar())
        }
    }

    fun changeHash(password : CharArray, hashType : String) {
        viewModelScope .launch {
            passWorking.value = true
            wrapEspressoIdlingResource {
                if(!repository.isPassValid(password, true)) {
                    passInvalid.value = Event(true)
                    passWorking.value = false
                    return@launch
                }

                if(!repository.createPassHash(password, hashType)) {
                    passFinish.value = Event(false)
                    passWorking.value = false
                    return@launch
                }
            }
            passFinish.value = Event(true)
            passWorking.value = false
            Arrays.fill(password, 0.toChar())
        }
    }
}