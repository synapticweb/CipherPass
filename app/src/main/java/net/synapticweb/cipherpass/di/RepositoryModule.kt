package net.synapticweb.cipherpass.di


import android.content.Context
import dagger.Module
import dagger.Provides
import net.synapticweb.cipherpass.DATABASE_FILE_NAME
import net.synapticweb.cipherpass.model.Repository
import net.synapticweb.cipherpass.model.RepositoryImpl
import javax.inject.Singleton

@Module
class RepositoryModule {
    @Singleton
    @Provides
    fun provideRepository(context: Context): Repository {
        return RepositoryImpl(context, DATABASE_FILE_NAME)
    }
}
