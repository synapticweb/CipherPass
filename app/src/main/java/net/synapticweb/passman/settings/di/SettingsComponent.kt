package net.synapticweb.passman.settings.di

import dagger.Subcomponent
import net.synapticweb.passman.settings.SettingsFragment
import net.synapticweb.passman.settings.SystemLockFragment

@Subcomponent(modules = [SettingsModule::class, SystemLockModule::class])
interface SettingsComponent {
    @Subcomponent.Factory
    interface Factory {
        fun create() : SettingsComponent
    }

    fun inject(fragment: SettingsFragment)
    fun inject(fragment: SystemLockFragment)
}