package net.synapticweb.passman.model

import androidx.lifecycle.LiveData

interface Repository {
    fun isUnlocked() : Boolean

    fun lock()

    suspend fun unlock(passphrase : ByteArray) : Boolean

    suspend fun isPassValid(passphrase: CharArray) : Boolean

    suspend fun createPassHash(passphrase : CharArray, newHashType : String?) : Boolean

    suspend fun reKey(passphrase: CharArray) : Boolean

    suspend fun insertEntry(entry : Entry) : Long

    suspend fun updateEntry(entry: Entry) : Int

    suspend fun deleteEntry(entry: Entry) : Int

    suspend fun getEntry(key : Long) : Entry

    fun getAllEntries() : LiveData<List<Entry>>

    fun removeDb()
}