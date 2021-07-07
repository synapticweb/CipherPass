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
import net.synapticweb.cipherpass.data.Repository
import net.synapticweb.cipherpass.util.CPCipher
import javax.inject.Singleton

@Component(modules = [AppSubcomponents::class, TestRepositoryModule::class, TestActivityViewModelModule::class,
    TestCipherModule::class,
    ViewModelBuilderModule::class])
@Singleton
interface TestAppComponent : AppComponent {
    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context, @BindsInstance application : Application): TestAppComponent
    }

    val repository : Repository
    val cipher : CPCipher
}