/*
 * Copyright (C) 2021 Eugen Rădulescu <synapticwebb@gmail.com>
 * This file is part of the CipherPass project ( https://github.com/synapticweb/CipherPass ).
 * See the LICENSE file in the project root for license terms.
 */

package net.synapticweb.cipherpass.di


import android.content.Context
import dagger.Module
import dagger.Provides
import net.synapticweb.cipherpass.util.CPCipher
import net.synapticweb.cipherpass.util.TestCPCipherImpl
import javax.inject.Singleton

@Module
class TestCipherModule {
    @Singleton
    @Provides
    fun providesCipher(context: Context) : CPCipher {
        return TestCPCipherImpl(context)
    }
}
