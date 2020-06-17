package net.synapticweb.passman.secretslist

import androidx.lifecycle.*
import kotlinx.coroutines.launch
import net.synapticweb.passman.Authorizer
import net.synapticweb.passman.Event
import net.synapticweb.passman.model.Repository
import net.synapticweb.passman.model.Secret
import javax.inject.Inject


class SecretsListViewModel @Inject constructor(private val repository: Repository) : ViewModel() {

    //Dacă repository nu este inițializat getAllSecrets întoarce LiveData<null>, ceea ce îi permite observerului
    //din fragment să apeleze fragmentul de autentificare.
    private val _secrets : LiveData<List<Secret>?> = repository.getAllSecrets()
    val secrets : LiveData<List<Secret>?> = _secrets

    private val _unauthorized = MutableLiveData<Event<Boolean>>()
    val unauthorized : LiveData<Event<Boolean>> = _unauthorized

    fun insertSecret() {
        if(!repository.isInitialized())
            return
        val secret = Secret("vasile_id", "vasile_pass")
        viewModelScope.launch {
            repository.insertSecret(secret)
        }
    }

    fun checkAuthorized() {
        if(Authorizer.timeOutExpired())
            _unauthorized.value = Event(true)
    }

    override fun onCleared() {
        super.onCleared()
        repository.closeDb()
    }
}