package net.synapticweb.passman.settings

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
import net.synapticweb.passman.*
import net.synapticweb.passman.util.EventObserver

class SettingsFragment : PreferenceFragmentCompat() {
    @Inject
    lateinit var viewModelFactory : ViewModelProvider.Factory
    private val lockState by activityViewModels<LockStateViewModel> {viewModelFactory}
    val viewModelFrg by viewModels<SettingsViewModel> {viewModelFactory}

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val parentActivity : Activity = context as Activity
        val app : CryptoPassApp = parentActivity.application as CryptoPassApp
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
        hashFunction?.summary = hashFunction?.entry

        val hashFuncEntries = requireActivity().resources.getStringArray(R.array.hash_func_entries)
        val hashFuncValues = requireActivity().resources.getStringArray(R.array.hash_func_values)

        hashFunction?.setOnPreferenceChangeListener { preference, newValue ->
                val index = hashFuncValues.indexOf(newValue)
                changeHash(preference as ListPreference, hashFuncEntries[index], newValue as String)
                false
            }
        }
}
