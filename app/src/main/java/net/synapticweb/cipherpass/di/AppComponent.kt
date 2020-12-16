package net.synapticweb.cipherpass.di

import android.app.Application
import android.content.Context
import dagger.BindsInstance
import dagger.Component
import net.synapticweb.cipherpass.MainActivity
import net.synapticweb.cipherpass.addeditentry.di.AddeditEntryComponent
import net.synapticweb.cipherpass.authenticate.di.AuthenticateComponent
import net.synapticweb.cipherpass.entrydetail.di.EntryDetailComponent
import net.synapticweb.cipherpass.entrieslist.di.EntriesListComponent
import net.synapticweb.cipherpass.settings.di.SettingsComponent
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
    fun entriesListComponent() : EntriesListComponent.Factory
    fun authenticateComponent() : AuthenticateComponent.Factory
    fun settingsComponent() : SettingsComponent.Factory
    fun addEntryComponent() : AddeditEntryComponent.Factory
    fun entryDetailComponent() : EntryDetailComponent.Factory
}