/*
 * Copyright (C) 2021 Eugen Rădulescu <synapticwebb@gmail.com>
 * This file is part of the CipherPass project ( https://github.com/synapticweb/CipherPass ).
 * See the LICENSE file in the project root for license terms.
 */

package net.synapticweb.cipherpass.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.synapticweb.cipherpass.model.IgnoredClient

@Database(entities = [IgnoredClient::class], version = 1, exportSchema = false)
abstract class UnencryptedDatabase : RoomDatabase() {
    abstract val dao : UnencryptedDao

    companion object {
        @Volatile
        private var INSTANCE: UnencryptedDatabase? = null
        fun getInstance(context: Context, scope: CoroutineScope): UnencryptedDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if(instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        UnencryptedDatabase::class.java,
                        "unencrypted.db")
                        .addCallback(DbCallback(context, scope))
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }


    private class DbCallback(
        private val context: Context,
        private val scope : CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let {
                scope.launch {
                    it.dao.insertIgnoredClient(IgnoredClient(context.packageName))
                }
            }
        }

    }
}