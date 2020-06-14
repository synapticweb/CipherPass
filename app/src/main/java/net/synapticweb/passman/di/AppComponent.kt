package net.synapticweb.passman.di

import android.app.Application
import android.content.Context
import dagger.BindsInstance
import dagger.Component
import net.synapticweb.passman.secretslist.di.SecretsListComponent
import javax.inject.Singleton

@Component(modules = [AppSubcomponents::class, RepositoryModule::class])
@Singleton
interface AppComponent {
    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context, @BindsInstance application : Application): AppComponent
    }

    fun secretsListComponent() : SecretsListComponent.Factory
}