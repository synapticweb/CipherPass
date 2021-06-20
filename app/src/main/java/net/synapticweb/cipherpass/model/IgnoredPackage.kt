/*
 * Copyright (C) 2021 Eugen Rădulescu <synapticwebb@gmail.com>
 * This file is part of the CipherPass project ( https://github.com/synapticweb/CipherPass ).
 * See the LICENSE file in the project root for license terms.
 */

package net.synapticweb.cipherpass.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ignored_packages")
data class IgnoredPackage(
    @ColumnInfo(name = "package") var mPackage : String
) {
    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    var id : Long = 0L
}
