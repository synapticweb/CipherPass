package net.synapticweb.cipherpass.di


import android.content.Context
import dagger.Module
import dagger.Provides
import net.synapticweb.cipherpass.model.Repository
import net.synapticweb.cipherpass.model.RepositoryImpl
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
