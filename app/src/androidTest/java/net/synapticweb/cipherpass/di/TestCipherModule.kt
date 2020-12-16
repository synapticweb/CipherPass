package net.synapticweb.cipherpass.di


import android.content.Context
import dagger.Module
import dagger.Provides
import net.synapticweb.cipherpass.util.CPCipher
import net.synapticweb.cipherpass.util.TestCPCipherImpl
import javax.inject.Singleton

@Module
class TestCipherModule {
    @Singleton
    @Provides
    fun providesCipher(context: Context) : CPCipher {
        return TestCPCipherImpl(context)
    }
}
