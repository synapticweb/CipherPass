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
    private val prefWrapper = PrefWrapper.getInstance(ApplicationProvider.getApplicationContext())
    val dataBindingIdlingResource = DataBindingIdlingResource()

    lateinit var repository: TestRepositoryImpl
    lateinit var cipher : TestCryptoPassCipher
    val application: CryptoPassApp = ApplicationProvider.getApplicationContext()

    fun setDb(hashType : String? = null) = runBlocking {
        val actHashType = hashType ?: prefWrapper.getString(HASH_TYPE_KEY) ?: HASH_PBKDF2
        repository.unlock(TEST_PASS.toByteArray())
        val salt = createSalt()
        val hashStr = repository.createHashString(TEST_PASS.toCharArray(), salt, actHashType)
        val hash = Hash(hashStr, byteArrayToHexStr(salt))
        repository.insertHash(hash)
    }


    override fun starting(description: Description?) {
        repository = (application.appComponent as TestAppComponent).repository as TestRepositoryImpl
        cipher = (application.appComponent as TestAppComponent).cipher as TestCryptoPassCipher

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