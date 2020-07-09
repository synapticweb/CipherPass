package net.synapticweb.passman.util

import java.io.File

interface CPCipher {
    fun encrypt(input : ByteArray) : ByteArray
    fun decrypt(input : ByteArray) : ByteArray
    fun isStorageHardwareBacked(): Boolean
    fun getEncryptedFilePath() : String
    suspend fun encryptPassToDisk(passphrase: CharArray) : Boolean
    suspend fun decryptPassFromDisk(encFile : File) : CharArray?
}