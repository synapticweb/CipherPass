package net.synapticweb.passman.settings

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.afollestad.materialdialogs.MaterialDialog
import javax.inject.Inject
import androidx.fragment.app.viewModels
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.setActionButtonEnabled
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.*
import net.synapticweb.passman.*


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
        lockState.unauthorized.observe(viewLifecycleOwner, EventObserver {
            findNavController().navigate(SettingsFragmentDirections.
                actionSettingsFragmentToAuthenticateFragment())
        })

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences)
        val appLock = findPreference<ListPreference>("applock")
        appLock?.summary = appLock?.entry

        val appLockEntries = requireActivity().resources.getStringArray(R.array.applock_entries)
        val appLockValues = requireActivity().resources.getStringArray(R.array.applock_values)
        appLock?.setOnPreferenceChangeListener { _, newValue ->
            val keygm = requireContext().getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            when(newValue){
                appLockValues[0] ->  {appLock.summary = appLockEntries[0]; true}

                appLockValues[1] -> {
                    if(Build.VERSION.SDK_INT < 23)
                        Snackbar.make(requireView(), getString(R.string.system_lock_not_available),
                            Snackbar.LENGTH_LONG).show()
                    else if(!keygm.isDeviceSecure)
                        Snackbar.make(requireView(), getString(R.string.system_lock_device_not_secure),
                            Snackbar.LENGTH_LONG).show()
                    else
                        findNavController().
                            navigate(SettingsFragmentDirections.
                            actionSettingsFragmentToSystemLockFragment())

                    false
                }

                appLockValues[2] -> {appLock.summary = appLockEntries[2]; true}

                else -> false
            }

        }

    }
}