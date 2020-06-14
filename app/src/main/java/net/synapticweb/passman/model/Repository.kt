package net.synapticweb.passman.model

import android.content.Context
import androidx.lifecycle.LiveData

interface Repository {
    fun initialize(context: Context, passphrase : ByteArray)

    fun closeDb()

    fun isInitialized() : Boolean

    suspend fun insertSecret(secret : Secret) : Long

    suspend fun updateSecret(secret: Secret) : Int

    suspend fun deleteSecret(secret: Secret) : Int

    suspend fun getSecret(key : Long) : Secret

    fun getAllSecrets() : LiveData<List<Secret>>
}