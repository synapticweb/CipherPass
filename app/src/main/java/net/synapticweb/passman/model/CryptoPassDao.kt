package net.synapticweb.passman.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.*

@Dao
interface CryptoPassDao {
    @Insert
    suspend fun insertSecret(secret : Secret) : Long

    @Update
    suspend fun updateSecret(secret : Secret) : Int

    @Delete
    suspend fun deleteSecret(secret : Secret) : Int

    @Query("SELECT * FROM `secrets` WHERE `id`= :key")
    suspend fun getSecret(key: Long) : Secret

    @Query("SELECT * FROM `secrets` ORDER BY `insertion_date` DESC")
    fun getAllSecrets() : LiveData<List<Secret>>

    @Insert
    suspend fun insertHash(hash : Hash) : Long

    @Update
    suspend fun updateHash(hash : Hash) : Int

    @Delete
    suspend fun deleteHash(hash : Hash) : Int

    @Query("SELECT * FROM `hash` WHERE `id`=1")
    suspend fun getHash() : Hash?
}