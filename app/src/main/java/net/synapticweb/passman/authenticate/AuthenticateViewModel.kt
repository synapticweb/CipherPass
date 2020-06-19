package net.synapticweb.passman.authenticate

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import net.synapticweb.passman.model.Repository
import javax.inject.Inject

const val PASSPHRASE_SET = "passphrase_set"

class AuthenticateViewModel @Inject constructor(
    private val repository: Repository, application: Application) : AndroidViewModel(application) {

    private val _passSet = MutableLiveData(run {
        val settings = PreferenceManager.getDefaultSharedPreferences(getApplication())
        settings.getBoolean(PASSPHRASE_SET, false)
    })
    val passSet : LiveData<Boolean> = _passSet

    val password = MutableLiveData("")
    val rePassword = MutableLiveData("")

    fun passMatch() : Boolean = password.value.equals(rePassword.value)

    fun setPassSet() {
        val settings = PreferenceManager.getDefaultSharedPreferences(getApplication())
        val editor = settings.edit()
        editor.putBoolean(PASSPHRASE_SET, true)
        editor.apply()
        editor.commit()
    }
}