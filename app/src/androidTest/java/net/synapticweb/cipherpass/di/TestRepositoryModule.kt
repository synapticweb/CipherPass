package net.synapticweb.cipherpass.di

import android.content.Context
import dagger.Module
import dagger.Provides
import net.synapticweb.cipherpass.TEST_DATABASE_NAME
import net.synapticweb.cipherpass.model.Repository
import net.synapticweb.cipherpass.model.TestRepositoryImpl
import javax.inject.Singleton

@Module
class TestRepositoryModule {
    @Singleton
    @Provides
    fun providesRepository(context: Context): Repository {
        return TestRepositoryImpl(context, TEST_DATABASE_NAME)
    }
}