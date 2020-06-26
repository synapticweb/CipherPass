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
import java.io.BufferedWriter
import java.io.FileWriter
import java.io.IOException
import javax.inject.Inject

class SystemLockViewModel @Inject constructor(private val repository: Repository, application: Application) :
    AndroidViewModel(application) {

    val working = MutableLiveData<Boolean>()
    val errorPassNoMatch = MutableLiveData<Boolean>()
    val storageSoft = MutableLiveData<Boolean>()
    val errorFileWriteFail = MutableLiveData<Boolean>()
    val finish = MutableLiveData<Boolean>()
    private lateinit var passSaved : String

    @ShouldTest
    fun encryptPassAndSetPref(passphrase: String? = null) {
        val toEncrypt = passphrase ?: passSaved
        val encrypted = CryptoPassCipher.encrypt(toEncrypt)
        val path = getApplication<CryptoPassApp>().filesDir.absolutePath + "/" + ENCRYPTED_PASS_FILENAME
        viewModelScope .launch {    //rulează pe threadul main
            withContext(Dispatchers.IO) {
                try {
                    val writer = BufferedWriter(FileWriter(path))
                    writer.write(encrypted)
                    writer.close()
                    setPref()
                }
                catch (exc : IOException) {
                    Log.e(APP_TAG, "Error writing the encrypted password: " + exc.message)
                    withContext(Dispatchers.Main) {
                        errorFileWriteFail.value = true
                    }
                    return@withContext
                } //TODO: de pus real logging
                withContext(Dispatchers.Main) {
                    finish.value = true
                }
            }
        }
    }

    private fun setPref() {
        val settings = PreferenceManager.getDefaultSharedPreferences(getApplication())
        val editor = settings.edit()
        editor.putString("applock", "system")
        editor.apply()
        editor.commit()
    }

    @ShouldTest
    fun validatePass(passphrase: String) {
         viewModelScope .launch {
            working.value = true
            val result = withContext(Dispatchers.Default) {
                val oldHash = repository.getHash()
                val newHash = byteArrayToHexStr(createHash(passphrase, hexStrToByteArray(oldHash.salt)))
                newHash == oldHash.hash
            }
            working.value = false
             if(!result) {
                 errorPassNoMatch.value = true
                 return@launch
             }

             if(!CryptoPassCipher.isStorageHardwareBacked()) {
                 storageSoft.value = true
                 passSaved = passphrase
                 return@launch
             }

             encryptPassAndSetPref(passphrase)
        }
    }
}