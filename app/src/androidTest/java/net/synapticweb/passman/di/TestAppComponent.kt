package net.synapticweb.passman.di

import android.app.Application
import android.content.Context
import dagger.BindsInstance
import dagger.Component
import net.synapticweb.passman.model.Repository
import net.synapticweb.passman.util.CPCipher
import net.synapticweb.passman.util.TestCryptoPassCipher
import javax.inject.Singleton

@Component(modules = [AppSubcomponents::class, TestRepositoryModule::class, LockStateModule::class,
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