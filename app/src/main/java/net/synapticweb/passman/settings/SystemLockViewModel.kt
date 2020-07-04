package net.synapticweb.passman.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import kotlinx.coroutines.*
import net.synapticweb.passman.*
import net.synapticweb.passman.model.Repository
import net.synapticweb.passman.util.*
import java.io.*
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

    private fun setPref() {
        val settings = PreferenceManager.getDefaultSharedPreferences(getApplication())
        val editor = settings.edit()
        editor.putString(APPLOCK_KEY, prefValue)
        editor.apply()
        editor.commit()
    }

    @ShouldTest
    fun validatePass(passphrase: CharArray) {
        viewModelScope.launch {
            working.value = true
            val result = repository.isPassValid(passphrase, false)
            working.value = false
            if (!result) {
                errorPassNoMatch.value = true
                return@launch
            }

            val encryptionResult = encryptPassToDisk(passphrase, cipher, true)
            if(!encryptionResult) {
                errorFileWriteFail.value = true
                return@launch
            }

            if (!cipher.isStorageHardwareBacked())
                storageSoft.value = true
            else {
                finish.value = true
                setPref()
            }
        }
    }

    fun onStorageSoftAccept() {
        setPref()
        finish.value = true
    }

    fun onStorageSoftRenounce() {
        val encFile = File(getApplication<CryptoPassApp>().filesDir.absolutePath + "/" +
                cipher.getEncryptedFilePath())
        if(encFile.exists())
            encFile.delete()
        finish.value = true
    }
}