package net.synapticweb.passman.model

import androidx.lifecycle.LiveData

interface Repository {
    fun isUnlocked() : Boolean

    fun lock()

    suspend fun unlock(passphrase : ByteArray) : Boolean

    suspend fun isPassValid(passphrase: CharArray, erasePass : Boolean) : Boolean

    suspend fun createPassHash(passphrase : CharArray) : Boolean

    suspend fun reKey(passphrase: CharArray) : Boolean

    suspend fun insertSecret(secret : Secret) : Long

    suspend fun updateSecret(secret: Secret) : Int

    suspend fun deleteSecret(secret: Secret) : Int

    suspend fun getSecret(key : Long) : Secret

    fun getAllSecrets() : LiveData<List<Secret>>

    suspend fun insertHash(hash: Hash) : Long

    suspend fun getHash() : Hash?

    suspend fun putHash(hash : String, salt : String) : Boolean
}