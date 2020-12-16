package net.synapticweb.cipherpass.util


interface CPCipher {
    fun encrypt(input : ByteArray) : ByteArray
    fun decrypt(input : ByteArray) : ByteArray
    fun isStorageHardwareBacked(): Boolean
    fun encryptPassToSettings(passphrase: CharArray) : Boolean
    fun decryptPassFromSettings() : CharArray?
}