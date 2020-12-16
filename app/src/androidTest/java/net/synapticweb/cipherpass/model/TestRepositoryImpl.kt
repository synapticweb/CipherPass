package net.synapticweb.cipherpass.model

import android.content.Context
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