package net.synapticweb.cipherpass.model

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery

@Dao
interface CipherPassDao {
    @Insert
    suspend fun insertEntry(entry : Entry) : Long

    @Update
    suspend fun updateEntry(entry : Entry) : Int

    @Delete
    suspend fun deleteEntry(entry : Entry) : Int

    @Query("SELECT * FROM `entries` WHERE `id`= :key")
    suspend fun getEntry(key: Long) : Entry

    @RawQuery(observedEntities = [Entry::class])
    fun getAllEntries(query : SupportSQLiteQuery) : LiveData<List<Entry>>

    @Query("SELECT * FROM `entries`")
    suspend fun getAllEntriesSync() : List<Entry>

    @Query("SELECT COUNT(*) FROM `entries`")
    suspend fun entriesCount() : Int

    @Insert
    suspend fun insertCustomField(field : CustomField) : Long

    @Update
    suspend fun updateCustomField(field: CustomField) : Int

    @Delete
    suspend fun deleteCustomField(field :  CustomField) : Int

    @Query("SELECT * FROM `custom_fields` WHERE `id`= :key")
    suspend fun getCustomField(key : Long) : CustomField

    @Query("SELECT * FROM `custom_fields` WHERE `entry`= :key")
    fun getCustomFields(key : Long) : LiveData<List<CustomField>>

    @Query("SELECT * FROM `custom_fields` WHERE `entry`= :key")
    suspend fun getCustomFieldsSync(key : Long) : List<CustomField>

    @Insert
    suspend fun insertHash(hash : Hash) : Long

    @Update
    suspend fun updateHash(hash : Hash) : Int

    @Delete
    suspend fun deleteHash(hash : Hash) : Int

    @Query("SELECT * FROM `hash` WHERE `id`=1")
    suspend fun getHash() : Hash?

    @RawQuery
    suspend fun queryDb(query : SupportSQLiteQuery) : List<Entry>
}