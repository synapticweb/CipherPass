package net.synapticweb.passman.model

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.preference.PreferenceManager
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SQLiteException
import net.sqlcipher.database.SupportFactory
import net.synapticweb.passman.*
import net.synapticweb.passman.util.*
import java.lang.Exception
import javax.inject.Inject

class RepositoryImpl @Inject constructor(
    private val context: Context,
    private val fileName : String) : Repository {
    private lateinit var database : CryptoPassDatabase

    override suspend fun unlock(passphrase: ByteArray) : Boolean {
        val factory = SupportFactory(passphrase, null, false)

        return withContext(Dispatchers.IO) {
            database = Room.databaseBuilder(
                context,
                CryptoPassDatabase::class.java,
                fileName
            )
                .openHelperFactory(factory)
                .build()
            //inițial am încercat să verific cu isOpen, dar întoarce false chiar dacă parola a fost
            // corectă. După o interogare reușită isOpen întoarce true.
            try {
                database.query("SELECT COUNT(*) FROM `secrets`", null)
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    override suspend fun isPassValid(passphrase: CharArray, erasePass : Boolean) : Boolean {
        val hashType = PreferenceManager.getDefaultSharedPreferences(context)
            .getString(HASH_TYPE_KEY, HASH_PBKDF2) ?: HASH_PBKDF2

       return withContext(Dispatchers.Default) {
           val oldHash = getHash()
           val newHash = createHashString(passphrase, hexStrToByteArray(oldHash!!.salt), hashType)
           newHash == oldHash.hash
       }
    }


    override suspend fun createPassHash(passphrase: CharArray, newHashType : String?) : Boolean {
        val hashType = newHashType ?:
            PreferenceManager.getDefaultSharedPreferences(context)
            .getString(HASH_TYPE_KEY, HASH_PBKDF2) ?: HASH_PBKDF2
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

    override suspend fun insertSecret(secret: Secret) : Long {
        return database.dao.insertSecret(secret)
    }

    override suspend fun updateSecret(secret: Secret): Int {
        return database.dao.updateSecret(secret)
    }

    override suspend fun deleteSecret(secret: Secret): Int {
        return database.dao.deleteSecret(secret)
    }

    override suspend fun getSecret(key: Long): Secret {
        return database.dao.getSecret(key)
    }

    override fun getAllSecrets(): LiveData<List<Secret>> {
        return database.dao.getAllSecrets()
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
                HASH_MD5_VALUE -> byteArrayToHexStr(
                    createHashMd5(charArrayToByteArray(passphrase), salt)
                )
                HASH_SHA_VALUE -> byteArrayToHexStr(
                    createHashSha(charArrayToByteArray(passphrase), salt)
                )
                HASH_PBKDF2 -> byteArrayToHexStr(
                    createHashPBKDF2(passphrase, salt)
                )
                else -> throw java.lang.IllegalArgumentException()
            }
        }
    }
}