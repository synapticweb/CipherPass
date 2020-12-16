package net.synapticweb.cipherpass.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import net.synapticweb.cipherpass.*
import net.synapticweb.cipherpass.model.Repository
import net.synapticweb.cipherpass.util.*
import java.util.*
import javax.inject.Inject

class SystemLockViewModel @Inject constructor(private val repository: Repository,
                                              private val cipher : CPCipher,
                                              application: Application) :
    AndroidViewModel(application) {

    val working = MutableLiveData<Boolean>()
    val errorPassNoMatch = MutableLiveData<Boolean>()
    val storageSoft = MutableLiveData<Boolean>()
    val errorFileWriteFail = MutableLiveData<Boolean>()
    val finish = MutableLiveData<Boolean>()
    lateinit var prefValue: String
    private val prefWrapper = PrefWrapper.getInstance(getApplication())


    fun validatePass(passphrase: CharArray) {
        viewModelScope.launch {
            working.value = true
            val result = wrapEspressoIdlingResource {
                repository.isPassValid(passphrase)
            }
            working.value = false
            if (!result) {
                errorPassNoMatch.value = true
                return@launch
            }

            val encryptionResult = wrapEspressoIdlingResource {
                cipher.encryptPassToSettings(passphrase)
            }
            if(!encryptionResult) {
                errorFileWriteFail.value = true
                return@launch
            }

            if (!cipher.isStorageHardwareBacked())
                storageSoft.value = true
            else {
                finish.value = true
                prefWrapper.setPref(APPLOCK_KEY, prefValue)
            }
            Arrays.fill(passphrase, 0.toChar())
        }
    }

    fun onStorageSoftAccept() {
        prefWrapper.setPref(APPLOCK_KEY, prefValue)
        finish.value = true
    }

    fun onStorageSoftRenounce() {
        prefWrapper.removePref(ENCRYPTED_PASS_KEY)
        finish.value = true
    }
}