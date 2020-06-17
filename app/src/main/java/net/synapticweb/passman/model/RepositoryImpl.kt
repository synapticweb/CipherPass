package net.synapticweb.passman.model

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Room
import net.sqlcipher.database.SupportFactory
import java.lang.Exception
import java.lang.IllegalStateException
import javax.inject.Inject

const val DATABASE_FILE_NAME = "passman.db"

class RepositoryImpl @Inject constructor(private val context: Context) : Repository {
    private var database : SecretsDatabase? = null

    override fun unlock(passphrase: ByteArray) : Boolean {
        val factory = SupportFactory(passphrase)
        database = Room.databaseBuilder(
            context,
            SecretsDatabase::class.java,
            DATABASE_FILE_NAME
        )
            .openHelperFactory(factory)
            .build()

        try {
            (database as SecretsDatabase).query("SELECT COUNT(*) FROM `secrets`", null)
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
        return database.secretsDao.insert(secret)
    }

    override suspend fun updateSecret(secret: Secret): Int {
        val database = database ?: throw IllegalStateException("Database locked")
        return database.secretsDao.update(secret)
    }

    override suspend fun deleteSecret(secret: Secret): Int {
        val database = database ?: throw IllegalStateException("Database locked")
        return database.secretsDao.delete(secret)
    }

    override suspend fun getSecret(key: Long): Secret {
        val database = database ?: throw IllegalStateException("Database locked")
        return database.secretsDao.get(key)
    }

    @Suppress("UNCHECKED_CAST")
    override fun getAllSecrets(): LiveData<List<Secret>?> {
        val database = database ?: return MutableLiveData(null)
        return database.secretsDao.getAllSecrets() as LiveData<List<Secret>?>
    }
}