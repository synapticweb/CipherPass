package net.synapticweb.passman.model

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import net.sqlcipher.database.SupportFactory
import net.synapticweb.passman.DATABASE_FILE_NAME
import java.lang.Exception
import java.lang.IllegalStateException
import javax.inject.Inject

class RepositoryImpl @Inject constructor(
    private val context: Context,
    private val fileName : String) : Repository {
    private lateinit var database : CryptoPassDatabase

    override suspend fun unlock(passphrase: ByteArray) : Boolean {
        val factory = SupportFactory(passphrase, null, false)
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

    override suspend fun getHash(): Hash {
        return database.dao.getHash()
    }
}