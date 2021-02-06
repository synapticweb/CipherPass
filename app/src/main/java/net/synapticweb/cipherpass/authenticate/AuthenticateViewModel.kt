/*
 * Copyright (C) 2021 Eugen Rădulescu <synapticwebb@gmail.com>
 * This file is part of the CipherPass project ( https://github.com/synapticweb/CipherPass ).
 * See the LICENSE file in the project root for license terms.
 */

package net.synapticweb.cipherpass.authenticate

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

const val IS_PASSPHRASE_SET_KEY = "passphrase_set_key"
const val AUTH_OK = 0
const val KEY_STORAGE_TYPE_KEY = "key_storage_type"
const val KEY_STORAGE_HARDWARE = "hardware"
const val KEY_STORAGE_SOFTWARE = "software"

class AuthenticateViewModel @Inject constructor(private val repository: Repository,
                                                private val cipher: CPCipher,
                                                application: Application) : AndroidViewModel(application) {

    private val prefWrapper = PrefWrapper.getInstance(getApplication())
    val passSet  = MutableLiveData(isPassSet())
    val passwd = MutableLiveData<CharArray?>()
    val working = MutableLiveData<Boolean>()
    val authResult = MutableLiveData<Event<Int>>()

    fun isPassSet() : Boolean {
        return prefWrapper.getBoolean(IS_PASSPHRASE_SET_KEY) != null
    }

    private fun setPassSet() {
        prefWrapper.setPref(IS_PASSPHRASE_SET_KEY, true)
    }

    fun getApplockPref() : String {
        val res = getApplication<CipherPassApp>().resources
        val prefWrapper = PrefWrapper.getInstance(getApplication())
        return prefWrapper.getString(res.getString(R.string.applock_key)) ?:
                res.getString(R.string.applock_passwd_value)
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

            if(prefWrapper.getString(KEY_STORAGE_TYPE_KEY) == null)
                prefWrapper.setPref(KEY_STORAGE_TYPE_KEY,  if(cipher.isStorageHardwareBacked())
                    KEY_STORAGE_HARDWARE
                else
                    KEY_STORAGE_SOFTWARE)
        }
    }
}
