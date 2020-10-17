package net.synapticweb.passman.util

import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.IdlingRegistry
import kotlinx.coroutines.runBlocking
import net.synapticweb.passman.*
import net.synapticweb.passman.di.TestAppComponent
import net.synapticweb.passman.model.Hash
import net.synapticweb.passman.model.TestRepositoryImpl
import org.junit.rules.TestWatcher
import org.junit.runner.Description

class CryptoPassTestRule : TestWatcher() {
    private lateinit var appPrefs : Map<String, *>
    val prefWrapper = PrefWrapper.getInstance(ApplicationProvider.getApplicationContext())
    val dataBindingIdlingResource = DataBindingIdlingResource()

    val application: CryptoPassApp = ApplicationProvider.getApplicationContext()
    val repository: TestRepositoryImpl = (application.appComponent as TestAppComponent).repository as TestRepositoryImpl
    val cipher : TestCryptoPassCipher = (application.appComponent as TestAppComponent).cipher as TestCryptoPassCipher

    fun setDb(hashType : String? = null) = runBlocking {
        val actHashType = hashType ?: prefWrapper.getString(HASH_TYPE_KEY) ?: HASH_PBKDF2
        repository.unlock(TEST_PASS.toByteArray())
        val salt = createSalt()
        val hashStr = repository.createHashString(TEST_PASS.toCharArray(), salt, actHashType)
        val hash = Hash(hashStr, byteArrayToHexStr(salt))
        repository.insertHash(hash)
    }


    override fun starting(description: Description?) {
        appPrefs = prefWrapper.getAll()

        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    override fun finished(description: Description?) {
        prefWrapper.restore(appPrefs)

        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)

        val dbFile = application.getDatabasePath(TEST_DATABASE_NAME)
        repository.lock() //rămîne un singur fișier
        if(dbFile.exists())
            dbFile.delete()

        repository.createPassHashFalse = false
        cipher.encryptPassReturnError = false
        cipher.hasHardwareStorage = false
        cipher.decryptPassReturnError = false
    }
}