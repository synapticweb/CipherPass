/*
 * Copyright (C) 2021 Eugen Rădulescu <synapticwebb@gmail.com>
 * This file is part of the CipherPass project ( https://github.com/synapticweb/CipherPass ).
 * See the LICENSE file in the project root for license terms.
 */

package net.synapticweb.cipherpass

import android.app.Application
import androidx.lifecycle.*
import net.synapticweb.cipherpass.model.Repository
import net.synapticweb.cipherpass.util.Event
import net.synapticweb.cipherpass.util.PrefWrapper
import javax.inject.Inject


class LockStateViewModel @Inject constructor(private val repository: Repository, application: Application)
    : AndroidViewModel(application), LifecycleObserver {

    var sleepTime : Long = 0L
    var lastBackPress : Long = 0L
    val unauthorized = MutableLiveData<Event<Boolean>>()
    private val res = getApplication<CipherPassApp>().resources

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onActivityPause() {
        val prefWrapper = PrefWrapper.getInstance(getApplication())
        val isTimeoutDisabled = prefWrapper.
            getString(res.getString(R.string.background_timeout_key)) ==
                res.getString(R.string.background_timeout_disabled_value)
        val isNoAuth = prefWrapper.getString(res.getString(R.string.applock_key)) ==
                res.getString(R.string.applock_nolock_value)
        if(isNoAuth || isTimeoutDisabled)
            return
        sleepTime = System.currentTimeMillis()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onActivityResume() {
        val prefWrapper = PrefWrapper.getInstance(getApplication())
        val timeout = prefWrapper.getString(res.getString(R.string.background_timeout_key))
        val isNoAuth = prefWrapper.getString(res.getString(R.string.applock_key)) ==
                res.getString(R.string.applock_nolock_value)
        //nu verificăm dacă suntem la pornirea aplicației, dacă este dezactivat sau dacă
        //aplicația nu necesită autentificare.
        timeout?. let {
            if (sleepTime == 0L || it == res.getString(R.string.background_timeout_disabled_value)
                || isNoAuth)
                return
            if (System.currentTimeMillis() - sleepTime > it.toLong() * 1000) {
                lockAndReauth()
            }
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

    fun lockAndReauth() {
        repository.lock()
        unauthorized.value = Event(true)
    }
}