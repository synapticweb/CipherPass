package net.synapticweb.passman.di

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import dagger.Module
import dagger.Provides
import net.synapticweb.passman.util.CPCipher
import net.synapticweb.passman.util.CryptoPassCipher
import javax.inject.Singleton

@Module
class CipherModule {
    @RequiresApi(Build.VERSION_CODES.M)
    @Singleton
    @Provides
    fun providesCipher(context: Context) : CPCipher {
        return CryptoPassCipher(context)
    }
}