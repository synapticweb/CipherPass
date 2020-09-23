package net.synapticweb.passman.model

import androidx.lifecycle.LiveData

interface Repository {
    fun isUnlocked() : Boolean

    fun lock()

    suspend fun unlock(passphrase : ByteArray) : Boolean

    suspend fun isPassValid(passphrase: CharArray) : Boolean

    suspend fun createPassHash(passphrase : CharArray, newHashType : String?) : Boolean

    suspend fun reKey(passphrase: CharArray) : Boolean

    suspend fun insertCredential(credential : Credential) : Long

    suspend fun updateCredential(credential: Credential) : Int

    suspend fun deleteCredential(credential: Credential) : Int

    suspend fun getCredential(key : Long) : Credential

    fun getAllCredentials() : LiveData<List<Credential>>

    fun removeDb()
}