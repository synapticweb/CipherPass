package net.synapticweb.passman.di

import android.app.Application
import android.content.Context
import dagger.BindsInstance
import dagger.Component
import net.synapticweb.passman.MainActivity
import net.synapticweb.passman.addeditcredential.di.AddeditCredComponent
import net.synapticweb.passman.authenticate.di.AuthenticateComponent
import net.synapticweb.passman.credentialslist.di.CredListComponent
import net.synapticweb.passman.settings.di.SettingsComponent
import javax.inject.Singleton

@Component(modules = [AppSubcomponents::class, RepositoryModule::class, LockStateModule::class,
    CipherModule::class,
    ViewModelBuilderModule::class])
@Singleton
interface AppComponent {
    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context, @BindsInstance application : Application): AppComponent
    }

    fun inject(activity : MainActivity)
    fun credListComponent() : CredListComponent.Factory
    fun authenticateComponent() : AuthenticateComponent.Factory
    fun settingsComponent() : SettingsComponent.Factory
    fun addCredentialComponent() : AddeditCredComponent.Factory
}