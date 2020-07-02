package net.synapticweb.passman.settings

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import kotlinx.coroutines.*
import net.synapticweb.passman.*
import net.synapticweb.passman.model.Repository
import net.synapticweb.passman.util.*
import java.io.*
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

    @ShouldTest
    fun encryptPass(passphrase: CharArray) {
        //byte array-ul rezultat e șters în encrypt()
        val encrypted = cipher.encrypt(charArrayToByteArray(passphrase))
        Arrays.fill(passphrase, 0.toChar())
        val path =
            getApplication<CryptoPassApp>().filesDir.absolutePath + "/" + cipher.getEncryptedFilePath()
        viewModelScope.launch {    //rulează pe threadul main
            withContext(Dispatchers.IO) {
                try {
                    wrapEspressoIdlingResource {
                        val stream = FileOutputStream(path)
                        stream.write(encrypted)
                        stream.close()
                    }
                } catch (exc: IOException) {
                    Log.e(APP_TAG, "Error writing the encrypted password: " + exc.message)
                    withContext(Dispatchers.Main) {
                        errorFileWriteFail.value = true
                    }
                    return@withContext
                } //TODO: de pus real logging
            }
        }
    }

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
            val result = withContext(Dispatchers.Default) {
                wrapEspressoIdlingResource {
                    val oldHash = repository.getHash()
                    val newHash = byteArrayToHexStr(
                        createHash(
                            //parola va fi necesară în encryptPass, deci avem false în ultimul parametru:
                            passphrase, hexStrToByteArray(oldHash.salt), false
                        )
                    )
                    newHash == oldHash.hash
                }
            }
            working.value = false
            if (!result) {
                errorPassNoMatch.value = true
                return@launch
            }

            encryptPass(passphrase)

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