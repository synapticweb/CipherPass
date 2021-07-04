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
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
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
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject


open class RepositoryImpl @Inject constructor(
    private val context: Context,
    private val fileName : String) : Repository {
    private var database : CipherPassDatabase? = null
    private  val prefWrapper = PrefWrapper.getInstance(context)
    private var lockId : UUID? = null

    override fun scheduleLock() {
        val prefWrapper = PrefWrapper.getInstance(context)
        val clipboardTimeout = prefWrapper.getString(context.resources
            .getString(R.string.clipboard_timeout_key))

        clipboardTimeout?.let {
            val timeoutDisabled = clipboardTimeout ==
                    context.resources.getString(R.string.clipboard_timeout_disabled_value)
            val noAuth =  prefWrapper.getString(context.resources.getString(R.string.applock_key)) ==
                    context.resources.getString(R.string.applock_nolock_value)

            if(timeoutDisabled || noAuth)
                return

            val lock = OneTimeWorkRequestBuilder<ScheduleLock>()
                .setInitialDelay(it.toLong() + 3, TimeUnit.SECONDS)
                .build()
            lockId = lock.id
            WorkManager.getInstance(context).enqueue(lock)
        }
    }

    override fun cancelScheduledLock() {
        lockId?.let {
            WorkManager.getInstance(context).cancelWorkById(it)
            lockId = null
        }
    }

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
                database!!.query("SELECT COUNT(*) FROM `entries`", null)
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
        if(database == null)
            throw SecurityException()

        return withContext(Dispatchers.IO) {
            try {
                (database!!.openHelper.writableDatabase as SQLiteDatabase).changePassword(passphrase)
            } catch (exc: SQLiteException) {
                return@withContext false
            }
            true
        }
    }

    override fun isUnlocked(): Boolean {
        return database?.isOpen ?: false
    }

    override fun lock() {
        database?.close()
        database = null
    }

    override suspend fun insertEntry(entry: Entry) : Long {
        database?. run {
            return database!!.dao.insertEntry(entry)
        } ?: throw  SecurityException()
    }

    override suspend fun updateEntry(entry: Entry): Int {
        database?. run {
            return database!!.dao.updateEntry(entry)
        } ?: throw  SecurityException()
    }

    override suspend fun deleteEntry(entry: Entry): Int {
        database?. run {
            return database!!.dao.deleteEntry(entry)
        } ?: throw  SecurityException()
    }

    override suspend fun getEntry(key: Long): Entry {
        database?. run {
            return database!!.dao.getEntry(key)
        } ?: throw  SecurityException()
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

        database?. run {
            return database!!.dao.getAllEntries(SimpleSQLiteQuery(query))
        } ?: throw  SecurityException()
    }

    override suspend fun getAllEntriesSync(): List<Entry> {
        database?. run {
            return database!!.dao.getAllEntriesSync()
        } ?: throw  SecurityException()
    }

    @VisibleForTesting
    suspend fun insertHash(hash: Hash): Long {
        database?. run {
            return database!!.dao.insertHash(hash)
        } ?: throw  SecurityException()
    }

    @VisibleForTesting
    suspend fun getHash(): Hash? {
        database?.run {
            return database!!.dao.getHash()
        } ?: throw  SecurityException()
    }

    @VisibleForTesting
    suspend fun putHash(hash: String, salt: String) : Boolean {
        if(database == null)
            throw SecurityException()

       return try {
            val currentHash = getHash()
            currentHash?.let {
                it.hash = hash
                it.salt = salt
                database!!.dao.updateHash(it)
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
        database?. run {
            return database!!.dao.queryDb(query)
        } ?: throw  SecurityException()
    }

    override suspend fun insertCustomField(field: CustomField): Long {
        database?. run {
            return database!!.dao.insertCustomField(field)
        } ?: throw  SecurityException()
    }

    override suspend fun updateCustomField(field: CustomField): Int {
        database?. run {
            return database!!.dao.updateCustomField(field)
        } ?: throw  SecurityException()
    }

    override suspend fun deleteCustomField(field: CustomField): Int {
        database?. run {
            return database!!.dao.deleteCustomField(field)
        } ?: throw  SecurityException()
    }

    override suspend fun getCustomField(key: Long): CustomField {
        database?. run {
            return database!!.dao.getCustomField(key)
        } ?: throw  SecurityException()
    }

    override fun getCustomFields(entry: Long): LiveData<List<CustomField>> {
        database?. run {
            return database!!.dao.getCustomFields(entry)
        } ?: throw  SecurityException()
    }

    override suspend fun getCustomFieldsSync(entry: Long): List<CustomField> {
        database?. run {
            return database!!.dao.getCustomFieldsSync(entry)
        } ?: throw  SecurityException()
    }

    override suspend fun dbContainsEntries() : Boolean {
        database?. run {
            return database!!.dao.entriesCount() > 0
        } ?: throw  SecurityException()
    }

    //Subclasele worker nu au voie să fie inner (și nici private), deci a trebuit să recurg
    //la dagger.
    class ScheduleLock(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {
        @Inject
        lateinit var repository : Repository

        init {
            (applicationContext as CipherPassApp).appComponent.inject(this)
        }

        override fun doWork(): Result {
            repository.lock()
            return Result.success()
        }
    }
}