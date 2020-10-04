package net.synapticweb.passman.credentialdetail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import net.synapticweb.passman.model.Credential
import net.synapticweb.passman.model.Repository
import net.synapticweb.passman.util.Event
import javax.inject.Inject

class CredDetailViewModel @Inject constructor(private val repository: Repository,
                                              application: Application
) : AndroidViewModel(application) {

    val credential = MutableLiveData<Credential>()
    val finishDeletion = MutableLiveData<Event<Boolean>>()

    fun getCredential(credId : Long) {
        viewModelScope.launch {
            credential.value = repository.getCredential(credId)
        }
    }

    fun delete() {
        credential.value?.let {
            viewModelScope.launch {
                repository.deleteCredential(it)
                finishDeletion.value = Event(true)
            }
        }
    }
}