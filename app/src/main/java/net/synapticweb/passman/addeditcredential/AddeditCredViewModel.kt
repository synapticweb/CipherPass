package net.synapticweb.passman.addeditcredential

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import net.synapticweb.passman.R
import net.synapticweb.passman.model.Credential
import net.synapticweb.passman.model.Repository
import net.synapticweb.passman.util.Event
import javax.inject.Inject

class AddeditCredViewModel @Inject constructor(private val repository: Repository,
                                               application: Application) :
    AndroidViewModel(application) {

    val name = MutableLiveData<String>()
    val username = MutableLiveData<String>()
    val password = MutableLiveData<String>()
    val url = MutableLiveData<String>()
    val comment = MutableLiveData<String>()
    val result = MutableLiveData<Event<Int>>()
    lateinit var savedCred : Credential

    fun populate(credId : Long) {
        viewModelScope.launch {
            repository.getCredential(credId). let { credential ->
                name.value = credential.accountName
                password.value = credential.password
                username.value = credential.accountId
                url.value = credential.url
                comment.value = credential.comment
                savedCred = credential
            }
        }
    }

    fun saveCredential(name : String,
                       username : String?,
                       password : String,
                       url : String?,
                       comment : String?,
                       credId : Long) {
        var dirty = false

        val credential = if (credId != 0L) savedCred
        else
            Credential()
        if(credential.accountName != name) {
            credential.accountName = name
            dirty = true
        }
        if(credential.accountId != username) {
            credential.accountId = username
            dirty = true
        }

        if(credential.password != password) {
            credential.password = password
            dirty = true
        }

        if(credential.url != url) {
            credential.url = url
            dirty = true
        }

        if(credential.comment != comment) {
            credential.comment = comment
            dirty = true
        }

        if(dirty) {
            viewModelScope.launch {
                if (credId != 0L) {
                    repository.updateCredential(credential)
                    result.value = Event(R.string.addedit_save_ok)
                }
                else {
                    repository.insertCredential(credential)
                    result.value = Event(0)
                }
            }
        }
        else
            result.value = Event(R.string.addedit_nochange)
    }
}