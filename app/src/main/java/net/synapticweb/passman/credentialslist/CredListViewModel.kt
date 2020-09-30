package net.synapticweb.passman.credentialslist

import android.app.Application
import androidx.lifecycle.*
import net.synapticweb.passman.CryptoPassApp
import net.synapticweb.passman.service.DatasetType
import net.synapticweb.passman.model.Repository
import net.synapticweb.passman.model.Credential
import javax.inject.Inject


class CredListViewModel @Inject constructor(private val repository: Repository, application: Application) :
    AndroidViewModel(application) {

    //Dacă repository nu este inițializat getAllSecrets întoarce LiveData<null>, ceea ce îi permite observerului
    //din fragment să apeleze fragmentul de autentificare.
    private val _credentials : LiveData<List<Credential>> = repository.getAllCredentials()
    val credentials : LiveData<List<Credential>> = _credentials

    fun insertSecret() {
        if(!repository.isUnlocked())
            return
//        val secret = Credential("vasile_id", "vasile_pass")
//        viewModelScope.launch {
//            repository.insertCredential(secret)
//        }
    }

    fun putAutofillData() {
        getApplication<CryptoPassApp>().autoFillData
            .putData("user", DatasetType.CREDENTIALS, "user", "pass")
    }

}