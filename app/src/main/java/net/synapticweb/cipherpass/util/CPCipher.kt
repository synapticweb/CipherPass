/*
 * Copyright (C) 2021 Eugen Rădulescu <synapticwebb@gmail.com>
 * This file is part of the CipherPass project ( https://github.com/synapticweb/CipherPass ).
 * See the LICENSE file in the project root for license terms.
 */

package net.synapticweb.cipherpass.util


interface CPCipher {
    fun encrypt(input : ByteArray) : ByteArray
    fun decrypt(input : ByteArray) : ByteArray
    suspend fun isStorageHardwareBacked(): Boolean
    fun encryptPassToSettings(passphrase: CharArray) : Boolean
    fun decryptPassFromSettings() : CharArray?
}