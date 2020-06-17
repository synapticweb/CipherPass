package net.synapticweb.passman.di

import dagger.Binds
import dagger.Module
import net.synapticweb.passman.model.Repository
import net.synapticweb.passman.model.RepositoryImpl
import javax.inject.Singleton

@Module
abstract class RepositoryModule {
    @Singleton
    @Binds
    abstract fun bindRepository(repo : RepositoryImpl) : Repository
}