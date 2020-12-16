package net.synapticweb.cipherpass.di

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import dagger.Module
import dagger.Provides
import net.synapticweb.cipherpass.util.CPCipher
import net.synapticweb.cipherpass.util.CPCipherImpl
import javax.inject.Singleton

@Module
class CipherModule {
    @RequiresApi(Build.VERSION_CODES.M)
    @Singleton
    @Provides
    fun providesCipher(context: Context) : CPCipher {
        return CPCipherImpl(context)
    }
}