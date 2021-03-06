/*
 * Copyright (C) 2021 Eugen Rădulescu <synapticwebb@gmail.com>
 * This file is part of the CipherPass project ( https://github.com/synapticweb/CipherPass ).
 * See the LICENSE file in the project root for license terms.
 */

package net.synapticweb.cipherpass.util

import androidx.lifecycle.Observer

//necesar pentru că în momentul în care un cîmp livedata este setat să zicem pe true
// (livedata poate fi inițializat fără a fi setat, e.g. private var _showSnackbarEvent = MutableLiveData<Boolean>()
//este apelat observerul. Dacă rămîne true, observerul nu știe ce să facă și rulează continuu - așa încît
//observerul verifică pentru null și cînd a fost gestionată schimbarea valorii se setează pe null și gata.
class Event<T> (private val content: T) {
    private var hasBeenHandled = false

    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }
}

class EventObserver<T>(private val onEventUnhandledContent: (T) -> Unit) : Observer<Event<T>> {
    override fun onChanged(event: Event<T>?) {
        event?.getContentIfNotHandled()?.let {
            onEventUnhandledContent(it)
        }
    }
}