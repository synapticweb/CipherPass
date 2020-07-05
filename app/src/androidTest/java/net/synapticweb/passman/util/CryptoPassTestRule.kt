package net.synapticweb.passman.util

import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.IdlingRegistry
import kotlinx.coroutines.runBlocking
import net.synapticweb.passman.*
import net.synapticweb.passman.di.TestAppComponent
import net.synapticweb.passman.model.Hash
import net.synapticweb.passman.model.Repository
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.io.File

class CryptoPassTestRule : TestWatcher() {
    private val settings : SharedPreferences = PreferenceManager.
            getDefaultSharedPreferences(ApplicationProvider.getApplicationContext())
    private lateinit var appPrefs : Map<String, *>
    val dataBindingIdlingResource = DataBindingIdlingResource()

    lateinit var repository: Repository
    val application: CryptoPassApp = ApplicationProvider.getApplicationContext()
    val encFile : File = File(application.filesDir.absolutePath + "/" + TEST_ENCRYPTED_PASS_FILENAME)


    fun setDb() = runBlocking {
        repository.unlock(TEST_PASS.toByteArray())
        val salt = createSalt()
        val hashStr = createHashString(TEST_PASS.toCharArray(), salt, settings.getString(
            HASH_TYPE_KEY, HASH_PBKDF2) ?: HASH_PBKDF2)
        val hash = Hash(hashStr, byteArrayToHexStr(salt))
        repository.insertHash(hash)
    }

    fun getBoolean(key: String): Boolean {
        return settings.getBoolean(key, false)
    }

    fun setBoolean(key: String, value: Boolean) {
        val editor = settings.edit()
        editor.putBoolean(key, value)
        editor.apply()
        editor.commit()
    }

    fun getString(key: String): String? {
        return settings.getString(key, null)
    }

    fun setString(key: String, value: String) {
        val editor = settings.edit()
        editor.putString(key, value)
        editor.apply()
        editor.commit()
    }

    fun removePref(key : String) {
        val editor = settings.edit()
        if(settings.contains(key))
            editor.remove(key)
        editor.apply()
        editor.commit()
    }

    override fun starting(description: Description?) {
        repository = (application.appComponent as TestAppComponent).repository

        appPrefs = settings.all

        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    override fun finished(description: Description?) {
        val editor = settings.edit()
        editor.clear()
        for(pref in appPrefs) {
            if(pref.value is Boolean)
                editor.putBoolean(pref.key, pref.value as Boolean)
            else if(pref.value is String)
                editor.putString(pref.key, pref.value as String)
        }
        editor.apply()
        editor.commit()

        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)

        val dbFile = application.getDatabasePath(TEST_DATABASE_NAME)
        if(dbFile.exists())
            dbFile.delete()

        if(encFile.exists())
            encFile.delete()
    }
}