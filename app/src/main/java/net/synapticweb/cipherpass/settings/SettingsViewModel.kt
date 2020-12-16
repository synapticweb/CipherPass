package net.synapticweb.cipherpass.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import net.synapticweb.cipherpass.APPLOCK_KEY
import net.synapticweb.cipherpass.APPLOCK_PASSWD_VALUE
import net.synapticweb.cipherpass.ENCRYPTED_PASS_KEY
import net.synapticweb.cipherpass.model.Repository
import net.synapticweb.cipherpass.util.*
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
    private val prefWrapper = PrefWrapper.getInstance(getApplication())

    fun hasEncryptedPass() : Boolean {
        return prefWrapper.getString(ENCRYPTED_PASS_KEY) != null
    }

    fun deleteEncryptedPass() {
        prefWrapper.removePref(ENCRYPTED_PASS_KEY)
    }

    private fun weakAuthentication() : Boolean {
        return prefWrapper.getString(APPLOCK_KEY) != APPLOCK_PASSWD_VALUE
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
                if(!repository.isPassValid(actualPass)) {
                    passInvalid.value = Event(true)
                    passWorking.value = false
                    return@launch
                }

                if (weakAuthentication() && !cipher.encryptPassToSettings(newPass)) {
                        deleteEncryptedPass()
                   prefWrapper.setPref(APPLOCK_KEY, APPLOCK_PASSWD_VALUE)
                    passFinish.value = Event(false)
                    passWorking.value = false
                    return@launch
                }

                if(!repository.createPassHash(newPass, null)) {
                    if(hasEncryptedPass()) {
                        prefWrapper.setPref(APPLOCK_KEY, APPLOCK_PASSWD_VALUE)
                        deleteEncryptedPass()
                    }
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
                if(!repository.isPassValid(password)) {
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