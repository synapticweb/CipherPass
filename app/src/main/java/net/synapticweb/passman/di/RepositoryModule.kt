package net.synapticweb.passman.di

import dagger.Module
import dagger.Provides
import net.synapticweb.passman.model.Repository
import net.synapticweb.passman.model.RepositoryImpl

@Module
class RepositoryModule {
    @Provides
    fun provideRepository() : Repository {
        return RepositoryImpl()
    }
}