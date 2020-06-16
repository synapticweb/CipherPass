package net.synapticweb.passman.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.*

@Dao
interface SecretsDao {
    @Insert
    suspend fun insert(secret : Secret) : Long

    @Update
    suspend fun update(secret : Secret) : Int

    @Delete
    suspend fun delete(secret : Secret) : Int

    @Query("SELECT * FROM `secrets` WHERE `id`= :key")
    suspend fun get(key: Long) : Secret

    @Query("SELECT * FROM `secrets` ORDER BY `insertion_date` DESC")
    fun getAllSecrets() : LiveData<List<Secret>>
}