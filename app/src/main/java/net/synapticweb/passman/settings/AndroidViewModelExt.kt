package net.synapticweb.passman.settings

import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.synapticweb.passman.APP_TAG
import net.synapticweb.passman.CryptoPassApp
import net.synapticweb.passman.util.*
import java.io.FileOutputStream
import java.io.IOException
import java.util.*


@ShouldTest
suspend fun AndroidViewModel.encryptPassToDisk(passphrase: CharArray, cipher : CPCipher,
                                               erasePass : Boolean) : Boolean {
    //byte array-ul rezultat e șters în encrypt()
    val encrypted = cipher.encrypt(charArrayToByteArray(passphrase))
    if(erasePass)
        Arrays.fill(passphrase, 0.toChar())
    val path =  getApplication<CryptoPassApp>().filesDir.absolutePath + "/" +
            cipher.getEncryptedFilePath()
     return  withContext(Dispatchers.IO) {
            try {
                val stream = FileOutputStream(path)
                stream.write(encrypted)
                stream.close()
            } catch (exc: IOException) {
                Log.e(APP_TAG, "Error writing the encrypted password: " + exc.message)
                return@withContext false
            }
         true
        }

}