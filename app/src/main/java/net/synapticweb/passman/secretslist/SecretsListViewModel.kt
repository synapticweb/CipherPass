package net.synapticweb.passman.secretslist

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import net.synapticweb.passman.model.Repository
import net.synapticweb.passman.model.Secret
import javax.inject.Inject


class SecretsListViewModel @Inject constructor(private val repository: Repository, application: Application) :
    AndroidViewModel(application) {

    //Dacă repository nu este inițializat getAllSecrets întoarce LiveData<null>, ceea ce îi permite observerului
    //din fragment să apeleze fragmentul de autentificare.
    private val _secrets : LiveData<List<Secret>?> = repository.getAllSecrets()
    val secrets : LiveData<List<Secret>?> = _secrets

    fun insertSecret() {
        if(!repository.isUnlocked())
            return
        val secret = Secret("vasile_id", "vasile_pass")
        viewModelScope.launch {
            repository.insertSecret(secret)
        }
    }

}