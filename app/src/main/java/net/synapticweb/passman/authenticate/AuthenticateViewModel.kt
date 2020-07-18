package net.synapticweb.passman.authenticate

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import net.synapticweb.passman.*
import net.synapticweb.passman.model.Repository
import net.synapticweb.passman.util.*
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
        val prefWrapper = PrefWrapper.getInstance(getApplication())
        return prefWrapper.getBoolean(PASSPHRASE_SET_KEY) != null
    }

    private fun setPassSet() {
        val prefWrapper = PrefWrapper.getInstance(getApplication())
        prefWrapper.setPref(PASSPHRASE_SET_KEY, true)
    }

    fun getApplockPref() : String {
        val prefWrapper = PrefWrapper.getInstance(getApplication())
        return prefWrapper.getString(APPLOCK_KEY) ?: APPLOCK_PASSWD_VALUE
    }

  fun getPassphrase()  {
        viewModelScope .launch {
           passwd.value = cipher.decryptPassFromSettings()
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
