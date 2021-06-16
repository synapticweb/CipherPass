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
import net.synapticweb.cipherpass.model.IgnoredClient

@Dao
interface UnencryptedDao {
    @Insert
    suspend fun insertIgnoredClient(ignoredClient: IgnoredClient) : Long

    @Delete
    suspend fun deleteIgnoredClient(ignoredClient: IgnoredClient) : Int

    @Query("SELECT * FROM `ignored_clients`")
    fun getIgnoredClients() : LiveData<List<IgnoredClient>>

    @Query("SELECT * FROM `ignored_clients`")
    suspend fun getIgnoredClientsSync() : List<IgnoredClient>
}