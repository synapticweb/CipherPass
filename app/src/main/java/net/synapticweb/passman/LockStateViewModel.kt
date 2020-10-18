package net.synapticweb.passman

import android.app.Application
import androidx.lifecycle.*
import net.synapticweb.passman.model.Repository
import net.synapticweb.passman.util.Event
import javax.inject.Inject

class LockStateViewModel @Inject constructor(private val repository: Repository, application: Application)
    : AndroidViewModel(application), LifecycleObserver {

    var sleepTime : Long = 0L
    var lastBackPress : Long = 0L

    val unauthorized = MutableLiveData<Event<Boolean>>()
    var isInUnlockActivity = false

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onActivityPause() {
        sleepTime = System.currentTimeMillis()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onActivityResume() {
        //nu verificăm dacă suntem la pornirea aplicației sau dacă ne întoarcem din activitatea unlock
        if (sleepTime == 0L || isInUnlockActivity)
            return
        if (System.currentTimeMillis() - sleepTime > 10000) {
            repository.lock()
            unauthorized.value = Event(true)
        }
    }

    override fun onCleared() {
        super.onCleared()
        repository.lock()
        lastBackPress = 0L
        sleepTime = 0L
    }

    fun isDbUnlocked() : Boolean{
        return repository.isUnlocked()
    }
}