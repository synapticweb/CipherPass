/*
 * Copyright (C) 2021 Eugen Rădulescu <synapticwebb@gmail.com>
 * This file is part of the CipherPass project ( https://github.com/synapticweb/CipherPass ).
 * See the LICENSE file in the project root for license terms.
 */

package net.synapticweb.cipherpass.di


import android.content.Context
import dagger.Module
import dagger.Provides
import net.synapticweb.cipherpass.data.Repository
import net.synapticweb.cipherpass.data.RepositoryImpl
import javax.inject.Singleton

const val DATABASE_FILE_NAME = "cipherpass.db"

@Module
class RepositoryModule {
    @Singleton
    @Provides
    fun provideRepository(context: Context): Repository {
        return RepositoryImpl(context, DATABASE_FILE_NAME)
    }
}
