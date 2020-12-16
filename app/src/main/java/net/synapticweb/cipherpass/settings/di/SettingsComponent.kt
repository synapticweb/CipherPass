package net.synapticweb.cipherpass.settings.di

import dagger.Subcomponent
import net.synapticweb.cipherpass.settings.SettingsFragment
import net.synapticweb.cipherpass.settings.EnterPasswordFragment

@Subcomponent(modules = [SettingsModule::class, SystemLockModule::class])
interface SettingsComponent {
    @Subcomponent.Factory
    interface Factory {
        fun create() : SettingsComponent
    }

    fun inject(fragment: SettingsFragment)
    fun inject(fragment: EnterPasswordFragment)
}