/*
 * Copyright (C) 2021 Eugen Rădulescu <synapticwebb@gmail.com>
 * This file is part of the CipherPass project ( https://github.com/synapticweb/CipherPass ).
 * See the LICENSE file in the project root for license terms.
 */

package net.synapticweb.cipherpass.autofill

import android.app.Application
import androidx.lifecycle.*
import net.synapticweb.cipherpass.data.Repository
import javax.inject.Inject

class MatchedEntriesViewModel @Inject constructor(
    private val repository: Repository,
    application: Application
): AndroidViewModel(application), LifecycleObserver {

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    fun checkDomainExists(webDomain : String) {

    }
}