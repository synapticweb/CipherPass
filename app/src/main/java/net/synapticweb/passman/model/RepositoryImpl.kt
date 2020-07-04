package net.synapticweb.passman.model

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SQLiteException
import net.sqlcipher.database.SupportFactory
import net.synapticweb.passman.DATABASE_FILE_NAME
import net.synapticweb.passman.util.*
import java.lang.Exception
import java.lang.IllegalStateException
import javax.inject.Inject

class RepositoryImpl @Inject constructor(
    private val context: Context,
    private val fileName : String) : Repository {
    private lateinit var database : CryptoPassDatabase

    override suspend fun unlock(passphrase: ByteArray) : Boolean {
        val factory = SupportFactory(passphrase)
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
        }
        catch (e : Exception) {return false}

        return true
    }

    override suspend fun isPassValid(passphrase: CharArray, erasePass : Boolean) : Boolean {
       return withContext(Dispatchers.Default) {
           wrapEspressoIdlingResource {
               val oldHash = getHash()
               val newHash = byteArrayToHexStr(
                   createHash(
                       //parola va fi necesară în encryptPass, deci avem false în ultimul parametru:
                       passphrase, hexStrToByteArray(oldHash!!.salt) )
               )
               newHash == oldHash.hash
           }
       }
    }

    override suspend fun createPassHash(passphrase: CharArray) {
        val salt = createSalt()
        val hash = withContext(Dispatchers.Default) {
            byteArrayToHexStr(
                createHash(passphrase, salt)
            )
        }

        withContext(Dispatchers.IO) {
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

    override suspend fun insertHash(hash: Hash): Long {
        return database.dao.insertHash(hash)
    }

    override suspend fun getHash(): Hash? {
        return database.dao.getHash()
    }

    override suspend fun putHash(hash: String, salt: String) {
        val currentHash = getHash()
        currentHash?. let {
            it.hash = hash
            it.salt = salt
            database.dao.updateHash(it)
        } ?: run {
            insertHash(Hash(hash, salt))
        }
    }
}