/*
 * Copyright (C) 2021 Eugen Rădulescu <synapticwebb@gmail.com>
 * This file is part of the CipherPass project ( https://github.com/synapticweb/CipherPass ).
 * See the LICENSE file in the project root for license terms.
 */

package net.synapticweb.cipherpass.model

import androidx.room.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@Entity(tableName = "custom_fields")
 data class CustomField (
    @Transient
    @ColumnInfo(name = "entry")
    var entry : Long = 0L,

    @ColumnInfo(name = "field_name")
    var fieldName : String,

    @ColumnInfo(name = "is_protected")
    var isProtected : Boolean = false,

    @ColumnInfo(name = "value")
    var value : String? = null
) {
    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    @Transient
    var id : Long = 0L

    @Ignore
    var inMemoryState : String? = null
}

@Fts4(contentEntity = CustomField::class)
@Entity(tableName = "custom_fields_fts")
class CustomFieldFts {
    @ColumnInfo(name = "field_name")
    var fieldName : String = ""

    @ColumnInfo(name = "value")
    var value : String = ""
}