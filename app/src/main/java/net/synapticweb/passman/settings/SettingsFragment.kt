package net.synapticweb.passman.settings

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import javax.inject.Inject
import androidx.fragment.app.viewModels
import androidx.preference.Preference
import com.google.android.material.snackbar.Snackbar
import net.synapticweb.passman.*
import net.synapticweb.passman.util.EventObserver


class SettingsFragment : PreferenceFragmentCompat() {
    @Inject
    lateinit var viewModelFactory : ViewModelProvider.Factory
    private val lockState by activityViewModels<LockStateViewModel> {viewModelFactory}
    private val viewModel by viewModels<SettingsViewModel> {viewModelFactory}

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val parentActivity : AppCompatActivity = context as AppCompatActivity
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

    private fun onPassphraseSelect(appLock: Preference, summaryEntry : String) :Boolean {
        viewModel.deletePasswdFile()
        appLock.summary = summaryEntry
        return true
    }

    private fun onSystemLockSelect(appLock: Preference, summaryEntry : String) : Boolean {
        val keygm = requireContext().getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        val isDeviceSecure = if(Build.VERSION.SDK_INT < 23)
                                keygm.isKeyguardSecure
                             else
                                keygm.isDeviceSecure
        if(!isDeviceSecure) {
            Snackbar.make(
                requireView(), getString(R.string.system_lock_device_not_secure),
                Snackbar.LENGTH_LONG
            ).show()
            return false
        }

        return if(!viewModel.hasPasswdFile()) {
            findNavController().navigate(
                SettingsFragmentDirections.actionSettingsFragmentToSystemLockFragment(
                    APPLOCK_SYSTEM_VALUE))
            false
        } else {
            appLock.summary = summaryEntry
            true
        }
    }

    private fun onNolockSelect(appLock: Preference, summaryEntry : String) : Boolean {
      return  if(!viewModel.hasPasswdFile()) {
            findNavController().navigate(
                SettingsFragmentDirections.
                actionSettingsFragmentToSystemLockFragment(APPLOCK_NOLOCK_VALUE))
            false
        }
        else {
          appLock.summary = summaryEntry
          true
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences)
        val appLock = findPreference<ListPreference>("applock")
        appLock?.summary = appLock?.entry

        val appLockEntries = requireActivity().resources.getStringArray(R.array.applock_entries)
        val appLockValues = requireActivity().resources.getStringArray(R.array.applock_values)

        appLock?.setOnPreferenceChangeListener { preference, newValue ->
        //returning true sets the preference, false does not set.
            when(newValue) {
                appLockValues[0] ->  onPassphraseSelect(preference, appLockEntries[0])

                appLockValues[1] -> onSystemLockSelect(preference, appLockEntries[1])

                appLockValues[2] -> onNolockSelect(preference, appLockEntries[2])

                else -> false
            }
        }
    }
}