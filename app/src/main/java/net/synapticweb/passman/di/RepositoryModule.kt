package net.synapticweb.passman.di

import android.content.Context
import dagger.Module
import dagger.Provides
import net.synapticweb.passman.Authorizer
import net.synapticweb.passman.model.Repository
import net.synapticweb.passman.model.RepositoryImpl

@Module
class RepositoryModule {
    @Provides
    fun provideRepository(context: Context) : Repository {
        val passphrase = Authorizer.getPassphrase()
        return RepositoryImpl(context, passphrase)
    }
}