package net.synapticweb.passman.model

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface CryptoPassDao {
    @Insert
    suspend fun insertEntry(entry : Entry) : Long

    @Update
    suspend fun updateEntry(entry : Entry) : Int

    @Delete
    suspend fun deleteEntry(entry : Entry) : Int

    @Query("SELECT * FROM `entries` WHERE `id`= :key")
    suspend fun getEntry(key: Long) : Entry

    @Query("SELECT * FROM `entries` ORDER BY `insertion_date` DESC")
    fun getAllEntries() : LiveData<List<Entry>>

    @Insert
    suspend fun insertHash(hash : Hash) : Long

    @Update
    suspend fun updateHash(hash : Hash) : Int

    @Delete
    suspend fun deleteHash(hash : Hash) : Int

    @Query("SELECT * FROM `hash` WHERE `id`=1")
    suspend fun getHash() : Hash?
}