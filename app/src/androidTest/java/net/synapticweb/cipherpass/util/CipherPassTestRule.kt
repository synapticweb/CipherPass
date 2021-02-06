package net.synapticweb.cipherpass.util

import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.IdlingRegistry
import kotlinx.coroutines.runBlocking
import net.synapticweb.cipherpass.*
import net.synapticweb.cipherpass.di.TestAppComponent
import net.synapticweb.cipherpass.model.Hash
import net.synapticweb.cipherpass.model.TestRepositoryImpl
import org.junit.rules.TestWatcher
import org.junit.runner.Description

const val TEST_DATABASE_NAME = "testcipherpass.db"
const val TEST_PASS = "test"

class CipherPassTestRule : TestWatcher() {
    private lateinit var appPrefs : Map<String, *>
    val prefWrapper = PrefWrapper.getInstance(ApplicationProvider.getApplicationContext())
    val dataBindingIdlingResource = DataBindingIdlingResource()

    val application: CipherPassApp = ApplicationProvider.getApplicationContext()
    val repository: TestRepositoryImpl = (application.appComponent as TestAppComponent).repository as TestRepositoryImpl
    val cipher : TestCPCipherImpl = (application.appComponent as TestAppComponent).cipher as TestCPCipherImpl

    fun setDb(hashType : String? = null) = runBlocking {
        val hashTypeKey = application.resources.getString(R.string.hash_type_key)
        val hashPk = application.resources.getString(R.string.hash_pbkdf2_value)
        val actHashType = hashType ?: prefWrapper.getString(hashTypeKey) ?: hashPk
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
        cipher.decryptPassReturnError = false
    }
}