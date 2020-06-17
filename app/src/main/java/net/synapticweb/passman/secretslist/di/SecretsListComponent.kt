package net.synapticweb.passman.secretslist.di

import dagger.Subcomponent
import net.synapticweb.passman.secretslist.SecretsListFragment

@Subcomponent(modules = [SecretsListModule::class])
interface SecretsListComponent {
    @Subcomponent.Factory
    interface Factory {
        fun create() : SecretsListComponent
    }

    fun inject(fragment: SecretsListFragment)
}