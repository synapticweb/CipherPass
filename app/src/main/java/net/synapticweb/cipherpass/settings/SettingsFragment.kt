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
import javax.inject.Inject
import androidx.fragment.app.viewModels
import androidx.preference.*
import net.synapticweb.cipherpass.*
import net.synapticweb.cipherpass.R
import net.synapticweb.cipherpass.util.EventObserver

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
        val appLock = findPreference<ListPreference>(getString(R.string.applock_key))
        appLock?.summary = appLock?.entry

        appLock?.setOnPreferenceChangeListener { preference, newValue ->
            //returning true sets the preference, false does not set.
            changeAuthentication(preference, newValue as String)
        }


        val changePassPref = findPreference<Preference>(getString(R.string.changepass_key))
        changePassPref?.setOnPreferenceClickListener {
            changePass()
            true
        }

        val hashFunction = findPreference<ListPreference>(getString(R.string.hash_type_key))
        hashFunction?.setOnPreferenceChangeListener { preference, newValue ->
                changeHash(preference as ListPreference, newValue as String)
                false
            }

        val allowReports = findPreference<CustomSwitchPreference>(getString(R.string.allow_reports_key))
        val allowUsage = findPreference<CustomSwitchPreference>(getString(R.string.allow_usage_data_key))

        allowReports?.apply {
            privLinkListener = View.OnClickListener {
                findNavController().navigate(
                    SettingsFragmentDirections.actionSettingsFragmentToPrivPolicyFragment()
                )

            }
        }

        allowUsage?.apply {
            privLinkListener = View.OnClickListener {
                findNavController().navigate(
                    SettingsFragmentDirections.actionSettingsFragmentToPrivPolicyFragment()
                )

            }
        }
    }
}
