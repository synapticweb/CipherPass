package net.synapticweb.cipherpass.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import net.synapticweb.cipherpass.authenticate.APPLOCK_KEY
import net.synapticweb.cipherpass.authenticate.APPLOCK_PASSWD_VALUE
import net.synapticweb.cipherpass.model.Repository
import net.synapticweb.cipherpass.util.*
import java.util.*
import javax.inject.Inject

class SettingsViewModel @Inject constructor(private val repository: Repository,
                                            private val cipher : CPCipher,
                                            application: Application) :
    AndroidViewModel(application) {

    val working = MutableLiveData<Boolean>()
    val finish = MutableLiveData<Event<Boolean>>()
    val passInvalid = MutableLiveData<Event<Boolean>>()
    val writeSettingsFail = MutableLiveData<Event<Boolean>>()

    private val prefWrapper = PrefWrapper.getInstance(getApplication())

    fun hasEncryptedPass() : Boolean {
        return prefWrapper.getString(ENCRYPTED_PASS_KEY) != null
    }

    fun deleteEncryptedPass() {
        prefWrapper.removePref(ENCRYPTED_PASS_KEY)
    }

    private fun hasWeakAuthentication() : Boolean {
        return prefWrapper.getString(APPLOCK_KEY) != APPLOCK_PASSWD_VALUE
    }

    fun changePass(actualPass : CharArray, newPass : CharArray) {
        viewModelScope.launch {
            working.value = true
            wrapEspressoIdlingResource {
                if(!repository.isPassValid(actualPass)) {
                    passInvalid.value = Event(true)
                    working.value = false
                    return@launch
                }

                //introducem în setări parola criptată doar dacă avem weak auth. Dacă scrierea
                //eșuează, scnhimbăm la auth cu parolă și nu mai facem nimic.
                if (hasWeakAuthentication() && !cipher.encryptPassToSettings(newPass)) {
                    deleteEncryptedPass()
                    prefWrapper.setPref(APPLOCK_KEY, APPLOCK_PASSWD_VALUE)
                    finish.value = Event(false)
                    working.value = false
                    return@launch
                }

                if(!repository.createPassHash(newPass, null)) {
                    if(hasEncryptedPass()) {
                        prefWrapper.setPref(APPLOCK_KEY, APPLOCK_PASSWD_VALUE)
                        deleteEncryptedPass()
                    }
                    finish.value = Event(false)
                    working.value = false
                    return@launch
                }
            }

            finish.value = Event(repository.reKey(newPass))
            working.value = false
            Arrays.fill(actualPass, 0.toChar())
            Arrays.fill(newPass, 0.toChar())
        }
    }

    fun changeHash(password : CharArray, hashType : String) {
        viewModelScope .launch {
            working.value = true
            wrapEspressoIdlingResource {
                if(!repository.isPassValid(password)) {
                    passInvalid.value = Event(true)
                    working.value = false
                    return@launch
                }

                if(!repository.createPassHash(password, hashType)) {
                    finish.value = Event(false)
                    working.value = false
                    return@launch
                }
            }
            finish.value = Event(true)
            working.value = false
            Arrays.fill(password, 0.toChar())
        }
    }

    fun changeAuthentication(passphrase : CharArray, authType : String) {
        viewModelScope.launch {
            working.value = true
            val result = wrapEspressoIdlingResource {
                repository.isPassValid(passphrase)
            }

            if (!result) {
                passInvalid.value = Event(true)
                working.value = false
                return@launch
            }

            val encryptionResult = wrapEspressoIdlingResource {
                cipher.encryptPassToSettings(passphrase)
            }
            if(!encryptionResult) {
                writeSettingsFail.value = Event(true)
                working.value = false
                return@launch
            }

            prefWrapper.setPrefSync(APPLOCK_KEY, authType)
            working.value = false
            finish.value = Event(true)
        }
    }

    fun shouldShowWarning() : Boolean {
        return !cipher.isStorageHardwareBacked() &&
                !(prefWrapper.getBoolean(DO_NOT_SHOW_WARNING) ?: false)
    }
}