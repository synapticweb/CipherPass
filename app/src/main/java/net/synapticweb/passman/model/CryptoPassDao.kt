package net.synapticweb.passman.model

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface CryptoPassDao {
    @Insert
    suspend fun insertCredential(credential : Credential) : Long

    @Update
    suspend fun updateCredential(credential : Credential) : Int

    @Delete
    suspend fun deleteCredential(credential : Credential) : Int

    @Query("SELECT * FROM `credentials` WHERE `id`= :key")
    suspend fun getCredential(key: Long) : Credential

    @Query("SELECT * FROM `credentials` ORDER BY `insertion_date` DESC")
    fun getAllCredentials() : LiveData<List<Credential>>

    @Insert
    suspend fun insertHash(hash : Hash) : Long

    @Update
    suspend fun updateHash(hash : Hash) : Int

    @Delete
    suspend fun deleteHash(hash : Hash) : Int

    @Query("SELECT * FROM `hash` WHERE `id`=1")
    suspend fun getHash() : Hash?
}