/*
 * Copyright (C) 2021 Eugen Rădulescu <synapticwebb@gmail.com>
 * This file is part of the CipherPass project ( https://github.com/synapticweb/CipherPass ).
 * See the LICENSE file in the project root for license terms.
 */

package net.synapticweb.cipherpass.di

import android.content.Context
import dagger.Module
import dagger.Provides
import net.synapticweb.cipherpass.model.Repository
import net.synapticweb.cipherpass.model.TestRepositoryImpl
import net.synapticweb.cipherpass.util.TEST_DATABASE_NAME
import javax.inject.Singleton

@Module
class TestRepositoryModule {
    @Singleton
    @Provides
    fun providesRepository(context: Context): Repository {
        return TestRepositoryImpl(context, TEST_DATABASE_NAME)
    }
}