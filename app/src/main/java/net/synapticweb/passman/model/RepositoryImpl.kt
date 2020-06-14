package net.synapticweb.passman.model

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton

@Singleton
class RepositoryImpl : Repository {
    private lateinit var database : SecretsDatabase

    override fun isInitialized() : Boolean {
        return this::database.isInitialized && database.isOpen
    }

    override fun initialize(context : Context, passphrase : ByteArray) {
        val factory = SupportFactory(passphrase)
        database = Room.databaseBuilder(context,
            SecretsDatabase::class.java,
            "passman.db")
            .openHelperFactory(factory)
            .build()
    }

    override fun closeDb() {
        database.close()
    }


    override suspend fun insertSecret(secret: Secret): Long {
        return database.secretsDao.insert(secret)
    }

    override suspend fun updateSecret(secret: Secret): Int {
        return database.secretsDao.update(secret)
    }

    override suspend fun deleteSecret(secret: Secret): Int {
        return database.secretsDao.delete(secret)
    }

    override suspend fun getSecret(key: Long): Secret {
        return database.secretsDao.get(key)
    }

    override fun getAllSecrets(): LiveData<List<Secret>> {
        return database.secretsDao.getAllSecrets()
    }
}