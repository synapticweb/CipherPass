package net.synapticweb.passman.model

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Room
import net.sqlcipher.database.SupportFactory
import java.lang.IllegalStateException
import javax.inject.Inject
import javax.inject.Singleton

const val DATABASE_FILE_NAME = "passman.db"

@Singleton
class RepositoryImpl @Inject constructor(context : Context, passphrase: ByteArray?) : Repository {
    private var database : SecretsDatabase? = null

    init {
        passphrase?. let {
            val factory = SupportFactory(it)
            database = Room.databaseBuilder(
                context,
                SecretsDatabase::class.java,
                DATABASE_FILE_NAME
            )
                .openHelperFactory(factory)
                .build()
        }
    }

    override fun closeDb() {
        database?.close()
    }

    override fun isInitialized(): Boolean {
        return database != null
    }

    override suspend fun insertSecret(secret: Secret) : Long {
        val database = database ?: throw IllegalStateException("Database not initialized")
        return database.secretsDao.insert(secret)
    }

    override suspend fun updateSecret(secret: Secret): Int {
        val database = database ?: throw IllegalStateException("Database not initialized")
        return database.secretsDao.update(secret)
    }

    override suspend fun deleteSecret(secret: Secret): Int {
        val database = database ?: throw IllegalStateException("Database not initialized")
        return database.secretsDao.delete(secret)
    }

    override suspend fun getSecret(key: Long): Secret {
        val database = database ?: throw IllegalStateException("Database not initialized")
        return database.secretsDao.get(key)
    }

    @Suppress("UNCHECKED_CAST")
    override fun getAllSecrets(): LiveData<List<Secret>?> {
        val database = database ?: return MutableLiveData(null)
        return database.secretsDao.getAllSecrets() as LiveData<List<Secret>?>
    }
}