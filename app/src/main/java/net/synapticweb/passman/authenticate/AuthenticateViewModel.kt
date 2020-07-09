package net.synapticweb.passman.authenticate

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import kotlinx.coroutines.*
import net.synapticweb.passman.*
import net.synapticweb.passman.model.Repository
import net.synapticweb.passman.util.*
import java.io.File
import java.util.*
import javax.inject.Inject


class AuthenticateViewModel @Inject constructor(private val repository: Repository,
                                                private val cipher: CPCipher,
                                                application: Application) : AndroidViewModel(application) {
    val passSet  = MutableLiveData(isPassSet())
    val passwd = MutableLiveData<CharArray?>()
    val working = MutableLiveData<Boolean>()
    val authResult = MutableLiveData<Event<Int>>()

    fun isPassSet() : Boolean {
        val settings = PreferenceManager.getDefaultSharedPreferences(getApplication())
        return settings.getBoolean(PASSPHRASE_SET_KEY, false)
    }

    private fun setPassSet() {
        val settings = PreferenceManager.getDefaultSharedPreferences(getApplication())
        val editor = settings.edit()
        editor.putBoolean(PASSPHRASE_SET_KEY, true)
        editor.apply()
        editor.commit()
    }

    fun getApplockPref() : String {
        val settings = PreferenceManager.getDefaultSharedPreferences(getApplication())
        return settings.getString(APPLOCK_KEY, APPLOCK_PASSWD_VALUE) ?: APPLOCK_PASSWD_VALUE
    }

  fun getPassphrase()  {
        val encFile = File(getApplication<CryptoPassApp>().filesDir.absolutePath + "/"
                + cipher.getEncryptedFilePath())

        viewModelScope .launch {
           passwd.value = cipher.decryptPassFromDisk(encFile)
        }
    }

    fun authenticate(passphrase: CharArray) {
        viewModelScope.launch {
            working.value = true
            val unlockResult = wrapEspressoIdlingResource {
                    repository.unlock(charArrayToByteArray(passphrase))
                }

            if(!unlockResult) {
                authResult.value = Event(R.string.pass_incorect)
                working.value = false
                return@launch
            }

            if(isPassSet()){
                authResult.value = Event(AUTH_OK)
                working.value = false
                return@launch
            }

            val createHashResult = wrapEspressoIdlingResource {
                repository.createPassHash(passphrase, null)
            }

            if(!createHashResult) {
                repository.lock()
                repository.removeDb()
                authResult.value = Event(R.string.error_setting_pass)
                working.value = false
                return@launch
            }

            setPassSet()
            working.value = false
            authResult.value = Event(AUTH_OK)
            Arrays.fill(passphrase, 0.toChar())
        }
    }
}
