/*
 * Copyright (C) 2021 Eugen Rădulescu <synapticwebb@gmail.com>
 * This file is part of the CipherPass project ( https://github.com/synapticweb/CipherPass ).
 * See the LICENSE file in the project root for license terms.
 */

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
import net.synapticweb.cipherpass.autofill.CipherPassService
import net.synapticweb.cipherpass.autofill.di.AutofillComponent
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
    fun autofillComponent() : AutofillComponent.Factory
}