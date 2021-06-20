/*
 * Copyright (C) 2021 Eugen Rădulescu <synapticwebb@gmail.com>
 * This file is part of the CipherPass project ( https://github.com/synapticweb/CipherPass ).
 * See the LICENSE file in the project root for license terms.
 */

package net.synapticweb.cipherpass.autofill

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import net.synapticweb.cipherpass.data.Repository
import net.synapticweb.cipherpass.util.Event
import javax.inject.Inject

class MatchedEntriesViewModel @Inject constructor(
    private val repository: Repository,
    application: Application
): AndroidViewModel(application) {

    val unauthorized = MutableLiveData<Event<Boolean>>()

    fun checkDomainExists(webDomain : String) {
        if(!repository.isUnlocked())
            unauthorized.value = Event(true)
    }
}