/*
 * Copyright (C) 2021 Eugen Rădulescu <synapticwebb@gmail.com>
 * This file is part of the CipherPass project ( https://github.com/synapticweb/CipherPass ).
 * See the LICENSE file in the project root for license terms.
 */

package net.synapticweb.cipherpass.data

import androidx.lifecycle.LiveData
import net.synapticweb.cipherpass.model.CustomField
import net.synapticweb.cipherpass.model.Entry

interface Repository {
    fun isUnlocked() : Boolean

    fun lock()

    suspend fun unlock(passphrase : ByteArray) : Boolean

    suspend fun isPassValid(passphrase: CharArray) : Boolean

    suspend fun createPassHash(passphrase : CharArray, newHashType : String?) : Boolean

    suspend fun reKey(passphrase: CharArray) : Boolean

    suspend fun insertEntry(entry : Entry) : Long

    suspend fun updateEntry(entry: Entry) : Int

    suspend fun deleteEntry(entry: Entry) : Int

    suspend fun getEntry(key : Long) : Entry

    fun getAllEntries(sortOrder: String) : LiveData<List<Entry>>

    suspend fun getAllEntriesSync() : List<Entry>

    fun removeDb()

    suspend fun queryDb(elements : List<String>) : List<Entry>

    suspend fun insertCustomField(field : CustomField) : Long

    suspend fun updateCustomField(field: CustomField) : Int

    suspend fun deleteCustomField(field : CustomField) : Int

    suspend fun getCustomField(key : Long) : CustomField

    fun getCustomFields(entry : Long) : LiveData<List<CustomField>>

    suspend fun getCustomFieldsSync(entry : Long) : List<CustomField>

    suspend fun dbContainsEntries() : Boolean
}