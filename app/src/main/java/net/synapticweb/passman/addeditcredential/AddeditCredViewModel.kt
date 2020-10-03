package net.synapticweb.passman.addeditcredential

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.synapticweb.passman.model.Credential
import net.synapticweb.passman.model.Repository
import javax.inject.Inject

class AddeditCredViewModel @Inject constructor(private val repository: Repository,
                                               application: Application) :
    AndroidViewModel(application) {

    val name = MutableLiveData<String>()
    val username = MutableLiveData<String>()
    val password = MutableLiveData<String>()
    val url = MutableLiveData<String>()
    val comment = MutableLiveData<String>()
    val working = MutableLiveData<Boolean>()

    fun populate(credId : Long) {
        viewModelScope.launch {
            repository.getCredential(credId). let { credential ->
                name.value = credential.accountName
                password.value = credential.password
                username.value = credential.accountId
                url.value = credential.url
                comment.value = credential.comment
            }
        }
    }

    fun saveCredential(name : String,
                       username : String?,
                       password : String,
                       url : String?,
                       comment : String?,
                       credId : Long?) {

        lateinit var credential : Credential
        viewModelScope.launch {
            working.value = true
            credential = if (credId != null)
                withContext(Dispatchers.IO) {
                    repository.getCredential(credId)
                }
            else
                Credential()

            credential.accountName = name
            credential.accountId = username
            credential.password = password
            credential.url = url
            credential.comment = comment

            if(credId != null)
                repository.updateCredential(credential)
            else
                repository.insertCredential(credential)
            working.value = false
        }
    }
}