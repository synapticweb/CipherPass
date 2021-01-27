package net.synapticweb.cipherpass.settings

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import javax.inject.Inject
import androidx.fragment.app.viewModels
import androidx.preference.Preference
import net.synapticweb.cipherpass.*
import net.synapticweb.cipherpass.authenticate.APPLOCK_KEY
import net.synapticweb.cipherpass.authenticate.HASH_TYPE_KEY
import net.synapticweb.cipherpass.util.EventObserver

const val CHANGE_PASS_KEY = "changepass"

class SettingsFragment : PreferenceFragmentCompat() {
    @Inject
    lateinit var viewModelFactory : ViewModelProvider.Factory
    private val lockState by activityViewModels<LockStateViewModel> {viewModelFactory}
    val viewModelFrg by viewModels<SettingsViewModel> {viewModelFactory}

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val parentActivity : Activity = context as Activity
        val app : CipherPassApp = parentActivity.application as CipherPassApp
        app.appComponent.settingsComponent().create().inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        lockState.unauthorized.observe(viewLifecycleOwner,
            EventObserver {
                findNavController().navigate(
                    SettingsFragmentDirections.actionSettingsFragmentToAuthenticateFragment()
                )
            })

        return super.onCreateView(inflater, container, savedInstanceState)
    }


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences)
        val appLock = findPreference<ListPreference>(APPLOCK_KEY)
        appLock?.summary = appLock?.entry

        appLock?.setOnPreferenceChangeListener { preference, newValue ->
            //returning true sets the preference, false does not set.
            changeAuthentication(preference, newValue as String)
        }


        val changePassPref = findPreference<Preference>(CHANGE_PASS_KEY)
        changePassPref?.setOnPreferenceClickListener {
            changePass()
            true
        }

        val hashFunction = findPreference<ListPreference>(HASH_TYPE_KEY)
        hashFunction?.setOnPreferenceChangeListener { preference, newValue ->
                changeHash(preference as ListPreference, newValue as String)
                false
            }
    }
}
