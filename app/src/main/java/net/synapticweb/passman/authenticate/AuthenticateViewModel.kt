package net.synapticweb.passman.authenticate

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import kotlinx.coroutines.*
import net.synapticweb.passman.PASSPHRASE_SET_KEY
import net.synapticweb.passman.byteArrayToHexStr
import net.synapticweb.passman.model.Hash
import net.synapticweb.passman.model.Repository
import java.security.SecureRandom
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject


class AuthenticateViewModel @Inject constructor(private val repository: Repository, application: Application) : AndroidViewModel(application) {

    private val _passSet = MutableLiveData(run {
        val settings = PreferenceManager.getDefaultSharedPreferences(getApplication())
        settings.getBoolean(PASSPHRASE_SET_KEY, false)
    })
    val passSet : LiveData<Boolean> = _passSet

    val password = MutableLiveData("")
    val rePassword = MutableLiveData("")

    fun passMatch() : Boolean = password.value.equals(rePassword.value)

    fun setPassSet() {
        val settings = PreferenceManager.getDefaultSharedPreferences(getApplication())
        val editor = settings.edit()
        editor.putBoolean(PASSPHRASE_SET_KEY, true)
        editor.apply()
        editor.commit()
    }

    private suspend fun createHash(passphrase: String, salt : ByteArray) : ByteArray {
        val spec = PBEKeySpec(passphrase.toCharArray(), salt, 65536, 128)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")

        var hash = ByteArray(16)
            withContext(Dispatchers.Default) {
                hash = factory.generateSecret(spec).encoded
            }
        return hash
    }

    fun createPassHash(passphrase : String) = runBlocking {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)

        val hash = byteArrayToHexStr(createHash(passphrase, salt))

        viewModelScope. launch {
            repository.insertHash(Hash(hash, byteArrayToHexStr(salt)))
        }
    }
}