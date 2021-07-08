/*
 * Copyright (C) 2021 Eugen Rădulescu <synapticwebb@gmail.com>
 * This file is part of the CipherPass project ( https://github.com/synapticweb/CipherPass ).
 * See the LICENSE file in the project root for license terms.
 */

package net.synapticweb.cipherpass.data

import android.content.Context
import net.synapticweb.cipherpass.data.RepositoryImpl
import javax.inject.Inject

class TestRepositoryImpl @Inject constructor(context: Context, fileName : String) :
        RepositoryImpl(context, fileName) {
    var createPassHashFalse : Boolean = false

    override suspend fun createPassHash(passphrase: CharArray, newHashType: String?): Boolean {
        if(createPassHashFalse)
            return false
        return super.createPassHash(passphrase, newHashType)
    }
}