package net.synapticweb.passman.authenticate

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import kotlinx.coroutines.*
import net.synapticweb.passman.PASSPHRASE_SET_KEY
import net.synapticweb.passman.ShouldTest
import net.synapticweb.passman.byteArrayToHexStr
import net.synapticweb.passman.createHash
import net.synapticweb.passman.model.Hash
import net.synapticweb.passman.model.Repository
import java.security.SecureRandom
import javax.inject.Inject


class AuthenticateViewModel @Inject constructor(private val repository: Repository, application: Application) : AndroidViewModel(application) {
    val password = MutableLiveData("")
    val rePassword = MutableLiveData("")
    val passSet  = MutableLiveData(isPassSet())

    fun isPassSet() : Boolean {
        val settings = PreferenceManager.getDefaultSharedPreferences(getApplication())
        return settings.getBoolean(PASSPHRASE_SET_KEY, false)
    }

    fun passMatch() : Boolean = password.value.equals(rePassword.value)

    fun setPassSet() {
        val settings = PreferenceManager.getDefaultSharedPreferences(getApplication())
        val editor = settings.edit()
        editor.putBoolean(PASSPHRASE_SET_KEY, true)
        editor.apply()
        editor.commit()
    }

@ShouldTest
 fun createPassHash(passphrase : String)  {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)

        viewModelScope .launch {
            val hash = withContext(Dispatchers.Default) {
                byteArrayToHexStr(createHash(passphrase, salt))
            }
            withContext(Dispatchers.IO) {
                repository.insertHash(Hash(hash, byteArrayToHexStr(salt)))
            }
        }
    }
}
