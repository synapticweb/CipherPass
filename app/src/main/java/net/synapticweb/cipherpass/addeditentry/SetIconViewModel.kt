/*
 * Copyright (C) 2021 Eugen Rădulescu <synapticwebb@gmail.com>
 * This file is part of the CipherPass project ( https://github.com/synapticweb/CipherPass ).
 * See the LICENSE file in the project root for license terms.
 */

package net.synapticweb.cipherpass.addeditentry

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject

class SetIconViewModel @Inject constructor(application: Application) :
    AndroidViewModel(application) {
    val icon = MutableLiveData<String>()

    fun setIcon(iconName : String) {
        icon.value = iconName
    }
}