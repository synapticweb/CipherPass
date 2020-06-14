package net.synapticweb.passman.model

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Secret::class], version = 1, exportSchema = true)
abstract class SecretsDatabase : RoomDatabase() {
    abstract val secretsDao : SecretsDao
}