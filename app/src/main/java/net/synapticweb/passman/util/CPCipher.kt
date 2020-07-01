package net.synapticweb.passman.util

interface CPCipher {
    fun encrypt(input : ByteArray) : ByteArray
    fun decrypt(input : ByteArray) : ByteArray
    fun isStorageHardwareBacked(): Boolean
    fun getEncryptedFilePath() : String
}