package net.synapticweb.passman.di

import android.app.Application
import android.content.Context
import dagger.BindsInstance
import dagger.Component
import net.synapticweb.passman.MainActivity
import net.synapticweb.passman.authenticate.di.AuthenticateComponent
import net.synapticweb.passman.secretslist.di.SecretsListComponent
import net.synapticweb.passman.settings.di.SettingsComponent
import javax.inject.Singleton

@Component(modules = [AppSubcomponents::class, AppModule::class, ViewModelBuilderModule::class])
@Singleton
interface AppComponent {
    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context, @BindsInstance application : Application): AppComponent
    }

    fun inject(activity : MainActivity)
    fun secretsListComponent() : SecretsListComponent.Factory
    fun authenticateComponent() : AuthenticateComponent.Factory
    fun settingsComponent() : SettingsComponent.Factory
}