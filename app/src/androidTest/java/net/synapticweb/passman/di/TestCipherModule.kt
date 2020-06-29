package net.synapticweb.passman.di


import android.content.Context
import dagger.Module
import dagger.Provides
import net.synapticweb.passman.util.CPCipher
import net.synapticweb.passman.util.CryptoPassCipher
import net.synapticweb.passman.util.TestCryptoPassCipher
import javax.inject.Singleton

@Module
class TestCipherModule {
    @Singleton
    @Provides
    fun providesCipher(context: Context) : CPCipher {
        return TestCryptoPassCipher(context)
    }
}
