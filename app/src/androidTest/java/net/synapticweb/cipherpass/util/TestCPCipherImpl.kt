/*
 * Copyright (C) 2021 Eugen Rădulescu <synapticwebb@gmail.com>
 * This file is part of the CipherPass project ( https://github.com/synapticweb/CipherPass ).
 * See the LICENSE file in the project root for license terms.
 */

package net.synapticweb.cipherpass.util

import android.content.Context

class TestCPCipherImpl(context: Context) : CPCipherImpl(context) {
    var encryptPassReturnError : Boolean = false
    var decryptPassReturnError : Boolean = false

    override fun encryptPassToSettings(passphrase: CharArray): Boolean {
        if(encryptPassReturnError)
            return false
        return super.encryptPassToSettings(passphrase)
    }

    override fun decryptPassFromSettings(): CharArray? {
        if(decryptPassReturnError)
            return null
        return super.decryptPassFromSettings()
    }
}