package net.synapticweb.passman.secretslist.di

import dagger.Subcomponent
import net.synapticweb.passman.di.FragmentScope
import net.synapticweb.passman.secretslist.SecretsListFragment

@Subcomponent(modules = [ViewModelFactoryModule::class])
@FragmentScope
interface SecretsListComponent {
    @Subcomponent.Factory
    interface Factory {
        fun create() : SecretsListComponent
    }

    fun inject(fragment: SecretsListFragment)
}