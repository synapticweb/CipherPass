package net.synapticweb.passman.model

import androidx.lifecycle.LiveData

interface Repository {
    fun isInitialized() : Boolean

    fun closeDb()

    suspend fun insertSecret(secret : Secret) : Long

    suspend fun updateSecret(secret: Secret) : Int

    suspend fun deleteSecret(secret: Secret) : Int

    suspend fun getSecret(key : Long) : Secret

    fun getAllSecrets() : LiveData<List<Secret>?>
}