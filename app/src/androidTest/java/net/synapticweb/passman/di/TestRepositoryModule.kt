package net.synapticweb.passman.di

import android.content.Context
import dagger.Module
import dagger.Provides
import net.synapticweb.passman.TEST_DATABASE_NAME
import net.synapticweb.passman.model.Repository
import net.synapticweb.passman.model.TestRepositoryImpl
import javax.inject.Singleton

@Module
class TestRepositoryModule {
    @Singleton
    @Provides
    fun providesRepository(context: Context): Repository {
        return TestRepositoryImpl(context, TEST_DATABASE_NAME)
    }
}