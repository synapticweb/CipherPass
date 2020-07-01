package net.synapticweb.passman.authenticate

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import kotlinx.coroutines.*
import net.synapticweb.passman.*
import net.synapticweb.passman.model.Hash
import net.synapticweb.passman.model.Repository
import net.synapticweb.passman.util.*
import java.io.DataInputStream
import java.io.File
import java.io.FileInputStream
import java.util.*
import javax.inject.Inject


class AuthenticateViewModel @Inject constructor(private val repository: Repository,
                                                private val cipher: CPCipher,
                                                application: Application) : AndroidViewModel(application) {
    val password = MutableLiveData<String>()
    val rePassword = MutableLiveData<String>()
    val passSet  = MutableLiveData(isPassSet())
    val passwd = MutableLiveData<ByteArray>()

    fun isPassSet() : Boolean {
        val settings = PreferenceManager.getDefaultSharedPreferences(getApplication())
        return settings.getBoolean(PASSPHRASE_SET_KEY, false)
    }

    fun passMatch() : Boolean = password.value == rePassword.value

    fun passEmpty() : Boolean = password.value?.isEmpty() ?: true

    fun setPassSet() {
        val settings = PreferenceManager.getDefaultSharedPreferences(getApplication())
        val editor = settings.edit()
        editor.putBoolean(PASSPHRASE_SET_KEY, true)
        editor.apply()
        editor.commit()
    }

    fun getApplockPref() : String {
        val settings = PreferenceManager.getDefaultSharedPreferences(getApplication())
        return settings.getString(APPLOCK_KEY, APPLOCK_PASSWD_VALUE) ?: APPLOCK_PASSWD_VALUE
    }

  fun getPassphrase()  {
        val encFile = File(getApplication<CryptoPassApp>().filesDir.absolutePath + "/"
                + cipher.getEncryptedFilePath())

        viewModelScope .launch {
            val encrypted : ByteArray = withContext(Dispatchers.IO) {
                val reader = DataInputStream(FileInputStream(encFile))
                val nBytesToRead: Int = reader.available()
                val bytes = ByteArray(nBytesToRead)
                if (nBytesToRead > 0)
                    reader.read(bytes)

                bytes
            }
            passwd.value = cipher.decrypt(encrypted)
        }
    }

@ShouldTest
 fun createPassHash(passphrase : CharArray) {
    val salt = createSalt()

        viewModelScope.launch {
            val hash = withContext(Dispatchers.Default) {
                byteArrayToHexStr(
                    createHash(passphrase, salt, true)
                )
            }
            withContext(Dispatchers.IO) {
                repository.insertHash(
                    Hash(
                        hash,
                        byteArrayToHexStr(salt)
                    )
                )
            }
            Arrays.fill(salt, 0.toByte())
        }
    }
}
