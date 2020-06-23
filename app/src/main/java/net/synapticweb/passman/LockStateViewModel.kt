package net.synapticweb.passman

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.synapticweb.passman.model.Repository
import javax.inject.Inject

class LockStateViewModel @Inject constructor(private val repository: Repository, application: Application)
    : AndroidViewModel(application) {

    var pressedOnce : Boolean = false
    var sleepTime : Long = 0L

    private val _unauthorized = MutableLiveData<Event<Boolean>>()
    val unauthorized : LiveData<Event<Boolean>> = _unauthorized

    fun unlockRepo(passphrase : String) : Boolean {
        return runBlocking(Dispatchers.Default) { //TODO: Greșit!
           repository.unlock(passphrase.toByteArray())
        }
    }

    fun setSleepTime() {
        sleepTime = System.currentTimeMillis()
    }

    fun checkIfAuthorized() {
        if(sleepTime == 0L) //nu verificăm dacă nu a fost încă minimizată activitatea
            return
        if(System.currentTimeMillis() - sleepTime > 30000) {
            repository.lock()
            _unauthorized.value = Event(true)
        }
    }

    fun lockRepo(){
        repository.lock()
    }

    override fun onCleared() {
        super.onCleared()
        repository.lock()
    }
}