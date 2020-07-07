package net.synapticweb.passman.settings

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.preference.ListPreference
import androidx.preference.Preference
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.getActionButton
import com.afollestad.materialdialogs.customview.customView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.synapticweb.passman.*
import net.synapticweb.passman.databinding.ChangePassDialogBinding
import net.synapticweb.passman.databinding.PasswdValidatorDialogBinding
import net.synapticweb.passman.util.*
import java.io.FileOutputStream
import java.io.IOException
import java.util.*


suspend fun AndroidViewModel.encryptPassToDisk(passphrase: CharArray, cipher : CPCipher) : Boolean {
    val passBytes = charArrayToByteArray(passphrase)
    val encrypted = cipher.encrypt(passBytes)
    val path =  getApplication<CryptoPassApp>().filesDir.absolutePath + "/" +
            cipher.getEncryptedFilePath()
    Arrays.fill(passBytes, 0.toByte())

     return  withContext(Dispatchers.IO) {
            try {
                val stream = FileOutputStream(path)
                stream.write(encrypted)
                stream.close()
            } catch (exc: IOException) {
                Log.e(APP_TAG, "Error writing the encrypted password: " + exc.message)
                return@withContext false
            }
         true
        }
}

fun SettingsFragment.changeHash(preference: ListPreference, newHashName : String, newHashType : String)  {
    val binding : PasswdValidatorDialogBinding =
        PasswdValidatorDialogBinding.inflate(
            LayoutInflater.from(requireContext())
        ).apply {
            viewModel = viewModelFrg
            lifecycleOwner = this@changeHash
        }

    MaterialDialog(requireContext()).show {
        noAutoDismiss()
        title(null, "Verify password")
        customView(null, binding.root)

        binding.passphrase.addTextChangedListener {
            binding.passLayout.error = null
        }

        positiveButton(android.R.string.ok) {
            if(binding.passphrase.text!!.isEmpty()) {
                binding.passLayout.error = getString(R.string.pass_empty)
                return@positiveButton
            }

            viewModelFrg.passWorking.removeObservers(viewLifecycleOwner)
            viewModelFrg.passWorking.observe(viewLifecycleOwner, Observer {
                getActionButton(WhichButton.POSITIVE).isEnabled = !it
            })

            viewModelFrg.passInvalid.removeObservers(viewLifecycleOwner)
            viewModelFrg.passInvalid.observe(viewLifecycleOwner,
                EventObserver {
                    binding.passLayout.error =
                        getString(R.string.pass_incorect)
                })

            viewModelFrg.passFinish.removeObservers(viewLifecycleOwner)
            viewModelFrg.passFinish.observe(viewLifecycleOwner,
                EventObserver {
                    binding.passphrase.text!!.clear()
                    dismiss()
                    if (it) {
                        viewModelFrg.setPref(newHashType)
                        preference.summary = newHashName
                        preference.value = newHashType
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.hash_change_success),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.hash_change_error),
                            Toast.LENGTH_SHORT
                        ).show()
                })

            viewModelFrg.changeHash(
                editableToCharArray(
                    binding.passphrase.text!!
                ), newHashType)
        }
    }
}

fun SettingsFragment.changePass() {
    MaterialDialog(requireContext()).show {
        noAutoDismiss()
        title(R.string.change_pass)
        val binding = ChangePassDialogBinding.inflate(
            LayoutInflater.from(requireContext())) .apply {
            model = viewModelFrg
            lifecycleOwner = this@changePass
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

        viewModelFrg.passWorking.observe(viewLifecycleOwner, Observer {
            getActionButton(WhichButton.POSITIVE).isEnabled = !it
        })

        viewModelFrg.passFinish.removeObservers(viewLifecycleOwner)
        viewModelFrg.passFinish.observe(viewLifecycleOwner, EventObserver {
            binding.actualPassphrase.text!!.clear()
            binding.newPassphrase.text!!.clear()
            binding.newPassphraseRetype.text!!.clear()
            dismiss()
            if(it)
                Toast.makeText(requireContext(), getString(R.string.change_pass_ok), Toast.LENGTH_SHORT).show()
            else
                Toast.makeText(requireContext(), getString(R.string.change_pass_error), Toast.LENGTH_SHORT).show()
        })

        viewModelFrg.passInvalid.removeObservers(viewLifecycleOwner)
        viewModelFrg.passInvalid.observe(viewLifecycleOwner, EventObserver {
            binding.passLayout.error = getString(R.string.pass_incorect)
        })

        viewModelFrg.passNoMatch.removeObservers(viewLifecycleOwner)
        viewModelFrg.passNoMatch.observe(viewLifecycleOwner, EventObserver {
            binding.newPassLayout.error = getString(R.string.pass_no_match)
        })
    }
}

fun SettingsFragment.changeAuthentication(preference: Preference, newValue : String) : Boolean {
    val appLockEntries = requireActivity().resources.getStringArray(R.array.applock_entries)
    val appLockValues = requireActivity().resources.getStringArray(R.array.applock_values)

    when(newValue) {
        appLockValues[0] -> { viewModelFrg.deletePasswdFile() //pashphrase
            preference.summary = appLockEntries[0]
            return true
        }

        //system lock
        appLockValues[1] -> {  val keygm = requireContext().getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
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
                        APPLOCK_SYSTEM_VALUE
                    ))
                false
            } else {
                preference.summary = appLockEntries[1]
                true
            }
        }

        //no lock
        appLockValues[2] -> {
            return  if(!viewModelFrg.hasPasswdFile()) {
                findNavController().navigate(
                    SettingsFragmentDirections.
                    actionSettingsFragmentToSystemLockFragment(APPLOCK_NOLOCK_VALUE))
                false
            }
            else {
                preference.summary = appLockEntries[2]
                true
            }
        }
        else -> return false
    }
}