package net.synapticweb.passman.model

import androidx.lifecycle.LiveData

interface Repository {
    fun isUnlocked() : Boolean

    fun lock()

    fun unlock(passphrase : ByteArray) : Boolean

    suspend fun insertSecret(secret : Secret) : Long

    suspend fun updateSecret(secret: Secret) : Int

    suspend fun deleteSecret(secret: Secret) : Int

    suspend fun getSecret(key : Long) : Secret

    fun getAllSecrets() : LiveData<List<Secret>>
}