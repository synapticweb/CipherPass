package net.synapticweb.passman.credentialslist

import android.app.Application
import androidx.lifecycle.*
import net.synapticweb.passman.model.Repository
import net.synapticweb.passman.model.Credential
import javax.inject.Inject


class CredListViewModel @Inject constructor(private val repository: Repository, application: Application) :
    AndroidViewModel(application) {

    //Dacă repository nu este inițializat getAllSecrets întoarce LiveData<null>, ceea ce îi permite observerului
    //din fragment să apeleze fragmentul de autentificare.
    private val _credentials : LiveData<List<Credential>> = repository.getAllCredentials()
    val credentials : LiveData<List<Credential>> = _credentials

}