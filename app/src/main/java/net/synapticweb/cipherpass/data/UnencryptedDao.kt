/*
 * Copyright (C) 2021 Eugen Rădulescu <synapticwebb@gmail.com>
 * This file is part of the CipherPass project ( https://github.com/synapticweb/CipherPass ).
 * See the LICENSE file in the project root for license terms.
 */

package net.synapticweb.cipherpass.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import net.synapticweb.cipherpass.model.IgnoredPackage

@Dao
interface UnencryptedDao {
    @Insert
    suspend fun insertIgnoredPackage(ignoredPackage: IgnoredPackage) : Long

    @Delete
    suspend fun deleteIgnoredPackage(ignoredPackage: IgnoredPackage) : Int

    @Query("SELECT * FROM `ignored_packages`")
    fun getIgnoredPackages() : LiveData<IgnoredPackage>
}