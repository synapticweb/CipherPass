package net.synapticweb.passman

import android.app.Application
import androidx.lifecycle.*
import net.synapticweb.passman.model.Repository
import net.synapticweb.passman.util.Event
import net.synapticweb.passman.util.PrefWrapper
import javax.inject.Inject

class LockStateViewModel @Inject constructor(private val repository: Repository, application: Application)
    : AndroidViewModel(application), LifecycleObserver {

    var lastBackPress : Long = 0L

    val unauthorized = MutableLiveData<Event<Boolean>>()

    //flag care arată că a fost pornită activitatea sistem de autentificare, deci onPause și onResume
    //nu ar trebui să mai memoreze și să verifice timestampuri. Este setat înainte de
    //startActivityForResult în onCreateView și după setare urmează un onResume. Nu poate fi resetat
    //în onActivityResult pentru că după aceea urmează un al doilea onResume.
    //Am ales să îl resetez în observerul pentru unlockSuccess.
    var startedUnlockActivity = false


    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onActivityPause() {
        if(!startedUnlockActivity) {
            val prefWrapper = PrefWrapper.getInstance(getApplication())
            prefWrapper.setPref(SLEEP_TIME_KEY, System.currentTimeMillis().toString())
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onActivityResume() {
        if(!startedUnlockActivity) {
            val prefWrapper = PrefWrapper.getInstance(getApplication())
            val sleepTime = prefWrapper.getString(SLEEP_TIME_KEY)
                ?: //nu verificăm dacă a trecut prin authenticate și nu a fost încă minimizată activitatea
                return

            if (System.currentTimeMillis() - sleepTime.toLong() > 10000) {
                repository.lock()
                unauthorized.value = Event(true)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        repository.lock()
        lastBackPress = 0L
        val prefWrapper = PrefWrapper.getInstance(getApplication())
        prefWrapper.removePref(SLEEP_TIME_KEY)
    }
}