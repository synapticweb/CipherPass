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
import java.io.BufferedWriter
import java.io.FileWriter
import java.io.IOException
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
    private lateinit var passSaved : String
    lateinit var prefValue : String

    @ShouldTest
    fun encryptPassAndSetPref(passphrase: String? = null) {
        val toEncrypt = passphrase ?: passSaved
        val encrypted = cipher.encrypt(toEncrypt)
        val path = getApplication<CryptoPassApp>().filesDir.absolutePath + "/" + cipher.getEncryptedFilePath()
        viewModelScope .launch {    //rulează pe threadul main
            withContext(Dispatchers.IO) {
                try {
                    wrapEspressoIdlingResource {
                        val writer = BufferedWriter(FileWriter(path))
                        writer.write(encrypted)
                        writer.close()
                    }
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
        editor.putString(APPLOCK_KEY, prefValue)
        editor.apply()
        editor.commit()
    }

    @ShouldTest
    fun validatePass(passphrase: String) {
            viewModelScope.launch {
                working.value = true
                val result = withContext(Dispatchers.Default) {
                    wrapEspressoIdlingResource {
                        val oldHash = repository.getHash()
                        val newHash = byteArrayToHexStr(
                            createHash(
                                passphrase,
                                hexStrToByteArray(oldHash.salt)
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

                if (!cipher.isStorageHardwareBacked()) {
                    storageSoft.value = true
                    passSaved = passphrase
                    return@launch
                }

                encryptPassAndSetPref(passphrase)
            }
        }
}