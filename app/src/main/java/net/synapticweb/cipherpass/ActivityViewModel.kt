/*
 * Copyright (C) 2021 Eugen Rădulescu <synapticwebb@gmail.com>
 * This file is part of the CipherPass project ( https://github.com/synapticweb/CipherPass ).
 * See the LICENSE file in the project root for license terms.
 */

package net.synapticweb.cipherpass

import android.app.Application
import androidx.lifecycle.*
import net.synapticweb.cipherpass.data.Repository
import net.synapticweb.cipherpass.util.Event
import javax.inject.Inject


class ActivityViewModel @Inject constructor(private val repository: Repository, application: Application)
    : AndroidViewModel(application), LifecycleObserver {

    var lastBackPress : Long = 0L
    val unauthorized = MutableLiveData<Event<Boolean>>()

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onActivityPause() {
        repository.scheduleLock()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onActivityResume() {
        if(!repository.isUnlocked())
            unauthorized.value = Event(true)
        repository.cancelScheduledLock()
    }

    fun isDbUnlocked() : Boolean {
        return repository.isUnlocked()
    }

    fun lockAndReauth() {
        repository.lock()
        unauthorized.value = Event(true)
    }
}