package net.synapticweb.passman.model

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import net.sqlcipher.database.SupportFactory
import net.synapticweb.passman.DATABASE_FILE_NAME
import java.lang.Exception
import java.lang.IllegalStateException
import javax.inject.Inject

class RepositoryImpl @Inject constructor(private val context: Context) : Repository {
    private var database : CryptoPassDatabase? = null

    override fun unlock(passphrase: ByteArray) : Boolean {
        val factory = SupportFactory(passphrase, null, false)
        database = Room.databaseBuilder(
            context,
            CryptoPassDatabase::class.java,
            DATABASE_FILE_NAME
        )
            .openHelperFactory(factory)
            .build()

        try {
            (database as CryptoPassDatabase).query("SELECT COUNT(*) FROM `secrets`", null)
        }
        catch (e : Exception) {return false}

        return true
    }

    override fun isUnlocked(): Boolean {
       return database != null
    }

    override fun lock() {
        database?.close()
        database = null
    }

    override suspend fun insertSecret(secret: Secret) : Long {
        val database = database ?: throw IllegalStateException("Database locked")
        return database.dao.insertSecret(secret)
    }

    override suspend fun updateSecret(secret: Secret): Int {
        val database = database ?: throw IllegalStateException("Database locked")
        return database.dao.updateSecret(secret)
    }

    override suspend fun deleteSecret(secret: Secret): Int {
        val database = database ?: throw IllegalStateException("Database locked")
        return database.dao.deleteSecret(secret)
    }

    override suspend fun getSecret(key: Long): Secret {
        val database = database ?: throw IllegalStateException("Database locked")
        return database.dao.getSecret(key)
    }

    override fun getAllSecrets(): LiveData<List<Secret>> {
        val database = database ?: throw IllegalStateException("Database locked")
        return database.dao.getAllSecrets()
    }

    override suspend fun insertHash(hash: Hash): Long {
        val database = database ?: throw IllegalStateException("Database locked")
        return database.dao.insertHash(hash)
    }

    override suspend fun getHash(): Hash {
        val database = database ?: throw IllegalStateException("Database locked")
        return database.dao.getHash()
    }
}