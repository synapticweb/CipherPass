/*
 * Copyright (C) 2021 Eugen Rădulescu <synapticwebb@gmail.com>
 * This file is part of the CipherPass project ( https://github.com/synapticweb/CipherPass ).
 * See the LICENSE file in the project root for license terms.
 */

package net.synapticweb.cipherpass.model

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Entry::class, CustomField::class, Hash::class, EntryFts::class, CustomFieldFts::class], version = 1, exportSchema = true)
abstract class CipherPassDatabase : RoomDatabase() {
    abstract val dao : CipherPassDao
}