package net.synapticweb.passman.settings

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import javax.inject.Inject
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.preference.Preference
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.getActionButton
import com.afollestad.materialdialogs.customview.customView
import com.google.android.material.snackbar.Snackbar
import net.synapticweb.passman.*
import net.synapticweb.passman.databinding.ChangePassDialogBinding
import net.synapticweb.passman.util.EventObserver
import net.synapticweb.passman.util.editableToCharArray
import net.synapticweb.passman.util.setupPasswordFields


class SettingsFragment : PreferenceFragmentCompat() {
    @Inject
    lateinit var viewModelFactory : ViewModelProvider.Factory
    private val lockState by activityViewModels<LockStateViewModel> {viewModelFactory}
    private val viewModelFrg by viewModels<SettingsViewModel> {viewModelFactory}

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

    private fun onPassphraseSelect(appLock: Preference, summaryEntry : String) : Boolean {
        viewModelFrg.deletePasswdFile()
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

        return if(!viewModelFrg.hasPasswdFile()) {
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
      return  if(!viewModelFrg.hasPasswdFile()) {
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

    private fun setupChangePass() {
        MaterialDialog(requireContext()).show {
            noAutoDismiss()
            title(R.string.change_pass)
            val binding = ChangePassDialogBinding.inflate(
                LayoutInflater.from(requireContext())) .apply {
                model = viewModelFrg
                lifecycleOwner = this@SettingsFragment
            }

            customView(null, binding.root)

            setupPasswordFields(
                binding.passLayout,
                arrayOf(
                    binding.actualPassphrase, binding.newPassphrase,
                    binding.newPassphraseRetype
                )
            )

            positiveButton(android.R.string.ok) {
                if(binding.actualPassphrase.text!!.isEmpty()) {
                    binding.passLayout.error = getString(R.string.act_pass_empty)
                    return@positiveButton
                }
                if(binding.newPassphrase.text!!.isEmpty()) {
                    binding.newPassLayout.error = getString(R.string.pass_empty)
                    return@positiveButton
                }

                val actPassCharArray = editableToCharArray(binding.actualPassphrase.text!!)
                val newPassCharArray = editableToCharArray(binding.newPassphrase.text!!)
                val reNewPassCharArray = editableToCharArray(binding.newPassphraseRetype.text!!)
                viewModelFrg.changePass(actPassCharArray, newPassCharArray, reNewPassCharArray)
            }

            binding.actualPassphrase.addTextChangedListener {
                binding.passLayout.error = null
            }

            binding.newPassphrase.addTextChangedListener {
                binding.newPassLayout.error = null
            }

            binding.newPassphraseRetype.addTextChangedListener {
                binding.newPassLayout.error = null
            }

            viewModelFrg.changePassWorking.observe(viewLifecycleOwner, Observer {
                getActionButton(WhichButton.POSITIVE).isEnabled = !it
            })

            viewModelFrg.changePassFinish.removeObservers(viewLifecycleOwner)
            viewModelFrg.changePassFinish.observe(viewLifecycleOwner, EventObserver {
                dismiss()
                if(it)
                    Toast.makeText(requireContext(), getString(R.string.change_pass_ok), Toast.LENGTH_SHORT).show()
                else
                    Toast.makeText(requireContext(), getString(R.string.change_pass_error), Toast.LENGTH_SHORT).show()
            })

            viewModelFrg.changePassInvalid.removeObservers(viewLifecycleOwner)
            viewModelFrg.changePassInvalid.observe(viewLifecycleOwner, EventObserver {
                binding.passLayout.error = getString(R.string.pass_incorect)
            })

            viewModelFrg.changePassNoMatch.removeObservers(viewLifecycleOwner)
            viewModelFrg.changePassNoMatch.observe(viewLifecycleOwner, EventObserver {
                binding.newPassLayout.error = getString(R.string.pass_no_match)
            })
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences)
        val appLock = findPreference<ListPreference>(APPLOCK_KEY)
        appLock?.summary = appLock?.entry

        val appLockEntries = requireActivity().resources.getStringArray(R.array.applock_entries)
        val appLockValues = requireActivity().resources.getStringArray(R.array.applock_values)

        appLock?.setOnPreferenceChangeListener { preference, newValue ->
            //returning true sets the preference, false does not set.
            when (newValue) {
                appLockValues[0] -> onPassphraseSelect(preference, appLockEntries[0])

                appLockValues[1] -> onSystemLockSelect(preference, appLockEntries[1])

                appLockValues[2] -> onNolockSelect(preference, appLockEntries[2])

                else -> false
            }
        }


        val changePassPref = findPreference<Preference>(CHANGE_PASS_KEY)
        changePassPref?.setOnPreferenceClickListener {
            setupChangePass()
            true
        }
    }
}