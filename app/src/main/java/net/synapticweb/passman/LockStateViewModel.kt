package net.synapticweb.passman

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*
import net.synapticweb.passman.model.Repository
import javax.inject.Inject

class LockStateViewModel @Inject constructor(private val repository: Repository, application: Application)
    : AndroidViewModel(application) {

    var pressedOnce : Boolean = false
    var sleepTime : Long = 0L

    val unauthorized = MutableLiveData<Event<Boolean>>()

    val working = MutableLiveData<Boolean>()
    val unlockSuccess = MutableLiveData<Event<Boolean>>()
    //nu pot să folosesc viewModelScope pentru că îi anulează job-ul automat în onClear, cînd apăs de 2 ori
    //back. Cînd repornesc aplicația blocul launch din unlockRepo nu se mai execută.
    private val uiScope = CoroutineScope(Dispatchers.Main + Job())

    fun unlockRepo(passphrase : String) {
        uiScope .launch {
            working.value = true
            val result =  withContext(Dispatchers.Default) {
                repository.unlock(passphrase.toByteArray())
            }
            working.value = false
            unlockSuccess.value = Event(result)
        }
    }

    fun setSleepTime() {
        sleepTime = System.currentTimeMillis()
    }

    fun checkIfAuthorized() {
        if(sleepTime == 0L) //nu verificăm dacă a trecut prin authenticate și nu a fost încă minimizată activitatea
            return
        if(System.currentTimeMillis() - sleepTime > 10000) {
            repository.lock()
            unauthorized.value = Event(true)
        }
    }


    override fun onCleared() {
        super.onCleared()
        repository.lock()
        pressedOnce = false
        sleepTime = 0
    }
}