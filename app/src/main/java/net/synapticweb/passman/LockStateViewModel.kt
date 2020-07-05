package net.synapticweb.passman

import android.app.Application
import androidx.lifecycle.*
import androidx.preference.PreferenceManager
import net.synapticweb.passman.model.Repository
import net.synapticweb.passman.util.Event
import javax.inject.Inject

class LockStateViewModel @Inject constructor(private val repository: Repository, application: Application)
    : AndroidViewModel(application), LifecycleObserver {

    var lastBackPress : Long = 0L
    private var sleepTime : Long = 0L

    val unauthorized = MutableLiveData<Event<Boolean>>()

    //flag care arată că a fost pornită activitatea sistem de autentificare, deci onPause și onResume
    //nu ar trebui să mai memoreze și să verifice timestampuri. Este setat înainte de
    //startActivityForResult în onCreateView și după setare urmează un onResume. Nu poate fi resetat
    //în onActivityResult pentru că după aceea urmează un al doilea onResume.
    //Am ales să îl resetez în observerul pentru unlockSuccess.
    var startedUnlockActivity = false



    private fun shouldManagePauseResume() : Boolean {
        if(startedUnlockActivity) {
            return false
        }
        val settings = PreferenceManager.getDefaultSharedPreferences(getApplication())
        return settings.getString(APPLOCK_KEY, APPLOCK_PASSWD_VALUE) != APPLOCK_NOLOCK_VALUE
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onActivityPause() {
        if(shouldManagePauseResume())
            sleepTime = System.currentTimeMillis()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onActivityResume() {
        if(shouldManagePauseResume()) {
            if (sleepTime == 0L) //nu verificăm dacă a trecut prin authenticate și nu a fost încă minimizată activitatea
                return
            if (System.currentTimeMillis() - sleepTime > 10000) {
                repository.lock()
                unauthorized.value = Event(true)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        repository.lock()
        lastBackPress = 0
        sleepTime = 0
    }
}