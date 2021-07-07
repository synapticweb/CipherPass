/*
 * Copyright (C) 2021 Eugen Rădulescu <synapticwebb@gmail.com>
 * This file is part of the CipherPass project ( https://github.com/synapticweb/CipherPass ).
 * See the LICENSE file in the project root for license terms.
 */

package net.synapticweb.cipherpass

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import net.synapticweb.cipherpass.data.Repository
import net.synapticweb.cipherpass.util.Event
import javax.inject.Inject


open class ActivityViewModel @Inject constructor(private val repository: Repository, application: Application)
    : AndroidViewModel(application), LifecycleObserver {

    val unauthorized = MutableLiveData<Event<Boolean>>()

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    open fun onAppBackgrounded() {
        repository.scheduleLock()
        Log.d(APP_TAG, "App stopped")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    open fun onAppForegrounded() {
        if(!repository.isUnlocked() )
            unauthorized.value = Event(true)

        repository.cancelScheduledLock()
        Log.d(APP_TAG, "App started")
    }

}