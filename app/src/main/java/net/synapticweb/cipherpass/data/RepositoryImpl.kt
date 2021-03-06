/*
 * Copyright (C) 2021 Eugen Rădulescu <synapticwebb@gmail.com>
 * This file is part of the CipherPass project ( https://github.com/synapticweb/CipherPass ).
 * See the LICENSE file in the project root for license terms.
 */

package net.synapticweb.cipherpass.data

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.room.Room
import androidx.sqlite.db.SimpleSQLiteQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SQLiteException
import net.sqlcipher.database.SupportFactory
import net.synapticweb.cipherpass.*
import net.synapticweb.cipherpass.entrieslist.*
import net.synapticweb.cipherpass.model.CustomField
import net.synapticweb.cipherpass.model.Entry
import net.synapticweb.cipherpass.model.Hash
import net.synapticweb.cipherpass.util.*
import java.lang.Exception
import java.lang.StringBuilder
import javax.inject.Inject


open class RepositoryImpl @Inject constructor(
    private val context: Context,
    private val fileName : String) : Repository {
    private lateinit var database : CipherPassDatabase
    private  val prefWrapper = PrefWrapper.getInstance(context)

    override suspend fun unlock(passphrase: ByteArray) : Boolean {
        val factory = SupportFactory(passphrase, null, false)

        return withContext(Dispatchers.IO) {
            database = Room.databaseBuilder(
                context,
                CipherPassDatabase::class.java,
                fileName
            )
                .openHelperFactory(factory)
                .build()
            //inițial am încercat să verific cu isOpen, dar întoarce false chiar dacă parola a fost
            // corectă. După o interogare reușită isOpen întoarce true.
            try {
                database.query("SELECT COUNT(*) FROM `entries`", null)
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    override suspend fun isPassValid(passphrase: CharArray) : Boolean {
        val hashType = prefWrapper.getString(context.resources.getString(R.string.hash_type_key))
            ?: context.resources.getString(R.string.hash_pbkdf2_value)

       return withContext(Dispatchers.Default) {
           val oldHash = getHash()
           val newHash = createHashString(passphrase, hexStrToByteArray(oldHash!!.salt), hashType)
           newHash == oldHash.hash
       }
    }


    override suspend fun createPassHash(passphrase: CharArray, newHashType : String?) : Boolean {
        val hashType = newHashType ?: prefWrapper.getString(context.resources.getString(R.string.hash_type_key))
        ?: context.resources.getString(R.string.hash_pbkdf2_value)
        val salt = createSalt()
        val hash = createHashString(passphrase, salt, hashType)

       return withContext(Dispatchers.IO) {
            putHash(
                hash,
                byteArrayToHexStr(salt)
            )
        }
    }

    override suspend fun reKey(passphrase: CharArray) : Boolean {
        return withContext(Dispatchers.IO) {
            try {
                (database.openHelper.writableDatabase as SQLiteDatabase).changePassword(passphrase)
            } catch (exc: SQLiteException) {
                return@withContext false
            }
            true
        }
    }

    override fun isUnlocked(): Boolean {
        return if(::database.isInitialized)
            database.isOpen
        else
            false
    }

    override fun lock() {
        if(::database.isInitialized)
            database.close()
    }

    override suspend fun insertEntry(entry: Entry) : Long {
        return database.dao.insertEntry(entry)
    }

    override suspend fun updateEntry(entry: Entry): Int {
        return database.dao.updateEntry(entry)
    }

    override suspend fun deleteEntry(entry: Entry): Int {
        return database.dao.deleteEntry(entry)
    }

    override suspend fun getEntry(key: Long): Entry {
        return database.dao.getEntry(key)
    }

    override fun getAllEntries(sortOrder: String) : LiveData<List<Entry>> {
        var query = "SELECT * FROM `entries` ORDER BY "
        query = when(sortOrder) {
            context.resources.getString(R.string.sort_creation_asc_name) -> "$query`insertion_date` ASC"
            context.resources.getString(R.string.sort_creation_desc_name) -> "$query`insertion_date` DESC"
            context.resources.getString(R.string.sort_name_asc_name) -> "$query`entry_name` ASC"
            context.resources.getString(R.string.sort_name_desc_name) -> "$query`entry_name` DESC"
            context.resources.getString(R.string.sort_modif_asc_name) -> "$query`modification_date` ASC"
            context.resources.getString(R.string.sort_modif_desc_name) -> "$query`modification_date` DESC"
            else -> "$query`insertion_date` DESC"
        }

        return database.dao.getAllEntries(SimpleSQLiteQuery(query))
    }

    override suspend fun getAllEntriesSync(): List<Entry> {
        return database.dao.getAllEntriesSync()
    }

    @VisibleForTesting
    suspend fun insertHash(hash: Hash): Long {
        return database.dao.insertHash(hash)
    }

    @VisibleForTesting
    suspend fun getHash(): Hash? {
        return database.dao.getHash()
    }

    @VisibleForTesting
    suspend fun putHash(hash: String, salt: String) : Boolean {
       return try {
            val currentHash = getHash()
            currentHash?.let {
                it.hash = hash
                it.salt = salt
                database.dao.updateHash(it)
            } ?: run {
                insertHash(Hash(hash, salt))
            }
           true
        }
        catch (exc : Exception) {
            false
        }
    }

    @VisibleForTesting
    suspend fun createHashString(passphrase: CharArray, salt : ByteArray, hashType : String) : String {
        return withContext(Dispatchers.Default) {
            when (hashType) {
                context.resources.getString(R.string.hash_md5_value) -> byteArrayToHexStr(
                    createHashMd5(charArrayToByteArray(passphrase), salt)
                )
                context.resources.getString(R.string.hash_sha_value) -> byteArrayToHexStr(
                    createHashSha(charArrayToByteArray(passphrase), salt)
                )
                context.resources.getString(R.string.hash_pbkdf2_value) -> byteArrayToHexStr(
                    createHashPBKDF2(passphrase, salt)
                )
                else -> throw java.lang.IllegalArgumentException()
            }
        }
    }

    override fun removeDb() {
        context.deleteDatabase(fileName)
    }

    override suspend fun queryDb(elements: List<String>) : List<Entry> {
        val queryStem = "SELECT * FROM `entries` WHERE `id` IN (SELECT `docid` FROM `entries_fts` WHERE `entries_fts` MATCH %s) OR `id` IN (SELECT `entry` FROM `custom_fields` JOIN `custom_fields_fts` ON `id`=`docid` WHERE `custom_fields_fts` MATCH %s)"

        val sb = StringBuilder()
        sb.append("'")
        for((index, element) in elements.withIndex()) {
            sb.append("$element*")
            if(index < elements.lastIndex)
                sb.append(" OR ")
        }
        sb.append("'")

        val query = SimpleSQLiteQuery(queryStem.format(sb.toString(), sb.toString()))
        return database.dao.queryDb(query)
    }

    override suspend fun insertCustomField(field: CustomField): Long {
        return database.dao.insertCustomField(field)
    }

    override suspend fun updateCustomField(field: CustomField): Int {
        return database.dao.updateCustomField(field)
    }

    override suspend fun deleteCustomField(field: CustomField): Int {
        return database.dao.deleteCustomField(field)
    }

    override suspend fun getCustomField(key: Long): CustomField {
       return database.dao.getCustomField(key)
    }

    override fun getCustomFields(entry: Long): LiveData<List<CustomField>> {
        return database.dao.getCustomFields(entry)
    }

    override suspend fun getCustomFieldsSync(entry: Long): List<CustomField> {
        return database.dao.getCustomFieldsSync(entry)
    }

    override suspend fun dbContainsEntries() : Boolean {
        return database.dao.entriesCount() > 0
    }
}