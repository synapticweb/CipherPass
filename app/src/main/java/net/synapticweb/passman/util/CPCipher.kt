package net.synapticweb.passman.util

interface CPCipher {
    fun encrypt(str : String) : String
    fun decrypt(str: String) : String
    fun isStorageHardwareBacked(): Boolean
    fun getEncryptedFilePath() : String
}