package net.synapticweb.passman.di

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import net.synapticweb.passman.LockStateViewModel
import net.synapticweb.passman.authenticate.AuthenticateViewModel
import net.synapticweb.passman.model.Repository
import net.synapticweb.passman.model.RepositoryImpl
import javax.inject.Singleton

@Module
abstract class AppModule {
    @Singleton
    @Binds
    abstract fun bindRepository(repo : RepositoryImpl) : Repository

    @Binds
    @IntoMap
    @Singleton
    @ViewModelKey(LockStateViewModel::class)
    abstract fun bindViewModel(viewModel : LockStateViewModel) : ViewModel
}