package net.synapticweb.cipherpass.settings.di

import dagger.Subcomponent
import net.synapticweb.cipherpass.settings.SettingsFragment

@Subcomponent(modules = [SettingsModule::class])
interface SettingsComponent {
    @Subcomponent.Factory
    interface Factory {
        fun create() : SettingsComponent
    }

    fun inject(fragment: SettingsFragment)
}