/*
 * Copyright (C) 2021 Eugen Rădulescu <synapticwebb@gmail.com>
 * This file is part of the CipherPass project ( https://github.com/synapticweb/CipherPass ).
 * See the LICENSE file in the project root for license terms.
 */

package net.synapticweb.cipherpass.settings

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.preference.ListPreference
import androidx.preference.Preference
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.getActionButton
import com.afollestad.materialdialogs.callbacks.onPreShow
import com.afollestad.materialdialogs.customview.customView
import com.google.android.material.snackbar.Snackbar
import net.synapticweb.cipherpass.*
import net.synapticweb.cipherpass.authenticate.KEY_STORAGE_HARDWARE
import net.synapticweb.cipherpass.authenticate.KEY_STORAGE_SOFTWARE
import net.synapticweb.cipherpass.authenticate.KEY_STORAGE_TYPE_KEY
import net.synapticweb.cipherpass.databinding.ChangePassDialogBinding
import net.synapticweb.cipherpass.databinding.PasswdValidatorDialogBinding
import net.synapticweb.cipherpass.databinding.SoftStorageWarningDialogBinding
import net.synapticweb.cipherpass.util.*
import java.util.*

const val DO_NOT_SHOW_WARNING_KEY = "do_ not_show_warning"

fun SettingsFragment.changeHash(preference: ListPreference, newHashType : String)  {
    doJobWithPassword(fun(dialog) {
        viewModelFrg.finish.removeObservers(viewLifecycleOwner)
        viewModelFrg.finish.observe(viewLifecycleOwner,
            EventObserver {
                dialog.dismiss()
                if (it) {
                    PrefWrapper.getInstance(requireContext()).
                    setPref(getString(R.string.hash_type_key), newHashType)
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

    }, fun(password) {
        viewModelFrg.changeHash(password, newHashType)
    })
}

fun SettingsFragment.changePass() {
    MaterialDialog(requireContext()).show {
        noAutoDismiss()
        title(R.string.change_pass_dialog_title)
        val binding = ChangePassDialogBinding.inflate(
            LayoutInflater.from(requireContext())) .apply {
            model = viewModelFrg
            lifecycleOwner = this@changePass
        }

        customView(null, binding.root)

        onPreShow { dialog ->
            disablePositiveWhenEmpty(dialog, R.id.actual_passphrase)
        }

        setupPasswordFields(
            binding.passLayout,
            arrayOf(
                binding.actualPassphrase, binding.newPassphrase,
                binding.newPassphraseRetype
            )
        )

        binding.actualPassphrase.addTextChangedListener {
            binding.passLayout.error = null
        }

        binding.newPassphrase.addTextChangedListener {
            binding.newPassLayout.error = null
        }

        viewModelFrg.working.removeObservers(viewLifecycleOwner)
        viewModelFrg.working.observe(viewLifecycleOwner, Observer {
            getActionButton(WhichButton.POSITIVE).isEnabled = !it
        })

        viewModelFrg.finish.removeObservers(viewLifecycleOwner)
        viewModelFrg.finish.observe(viewLifecycleOwner, EventObserver {
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

        positiveButton(android.R.string.ok) {
            val newPass = editableToCharArray(binding.newPassphrase.text!!)
            val reNewPass = editableToCharArray(binding.newPassphraseRetype.text!!)

            if(binding.newPassphrase.text!!.isEmpty()) {
                binding.newPassLayout.error = getString(R.string.pass_empty)
                Arrays.fill(reNewPass, 0.toChar())
                return@positiveButton
            }

            if(!newPass.contentEquals(reNewPass)) {
                binding.newPassLayout.error = getString(R.string.pass_no_match)
                Arrays.fill(reNewPass, 0.toChar())
                return@positiveButton
            }

            Arrays.fill(reNewPass, 0.toChar())
            val actualPass = editableToCharArray(binding.actualPassphrase.text!!)
            viewModelFrg.changePass(actualPass, newPass)
        }
    }
}

fun SettingsFragment.changeAuthentication(preference: Preference, newValue : String) : Boolean {
    val appLockEntries = requireActivity().resources.getStringArray(R.array.applock_entries)
    val appLockValues = requireActivity().resources.getStringArray(R.array.applock_values)

    when(newValue) { //pashphrase
        appLockValues[0] -> {
            viewModelFrg.deleteEncryptedPass()
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

            return if(!viewModelFrg.hasEncryptedPass()) {
                createDialogForAuthChange(preference, appLockEntries[1], appLockValues[1])
                false
            } else {
                preference.summary = appLockEntries[1]
                true
            }
        }

        //no lock
        appLockValues[2] -> {
            return  if(!viewModelFrg.hasEncryptedPass()) {
                createDialogForAuthChange(preference, appLockEntries[2], appLockValues[2])
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

fun SettingsFragment.createDialogForAuthChange(preference: Preference, authName: String,
                                               authenticationType : String) {
    val setObservers = fun(dialog : MaterialDialog) {
        viewModelFrg.finish.removeObservers(viewLifecycleOwner)
        viewModelFrg.finish.observe(viewLifecycleOwner, EventObserver {
            preference.summary = authName
            (preference as ListPreference).value = authenticationType
            dialog.dismiss()
        })

        viewModelFrg.writeSettingsFail.removeObservers(viewLifecycleOwner)
        viewModelFrg.writeSettingsFail.observe(viewLifecycleOwner, EventObserver {
            dialog.dismiss()
            Snackbar.make(requireView(), getString(R.string.system_lock_file_write_fail),
                Snackbar.LENGTH_SHORT).show()
        })
    }

    val onPositive = fun(password : CharArray) {
        viewModelFrg.changeAuthentication(password, authenticationType)
    }

    if(shouldShowWarning()) {
        MaterialDialog(requireContext()).show {
            noAutoDismiss()
            title(R.string.warning_title)
            val binding = SoftStorageWarningDialogBinding.inflate(
                LayoutInflater.from(requireContext())).apply {
                    authType = authenticationType
                    lifecycleOwner = this@createDialogForAuthChange
                }
            customView(null, binding.root)
            positiveButton(android.R.string.ok) {
                if(binding.stopShowingWarning.isChecked)
                    PrefWrapper.getInstance(requireContext()).setPref(DO_NOT_SHOW_WARNING_KEY, true)
                dismiss()
                doJobWithPassword(setObservers, onPositive)
            }

            negativeButton(android.R.string.cancel) {
                dismiss()
            }
        }
    }
    else
        doJobWithPassword(setObservers, onPositive)
}

fun SettingsFragment.doJobWithPassword(setCustomObservers : (dialog : MaterialDialog) -> Unit,
                                       onPositive : (password : CharArray) -> Unit) {
    val binding : PasswdValidatorDialogBinding =
        PasswdValidatorDialogBinding.inflate(
            LayoutInflater.from(requireContext())
        ).apply {
            viewModel = viewModelFrg
            lifecycleOwner = this@doJobWithPassword
        }

    MaterialDialog(requireContext()).show {
        noAutoDismiss()
        title(null, getString(R.string.enter_pass))
        customView(null, binding.root)

        onPreShow { dialog ->
            disablePositiveWhenEmpty(dialog, R.id.passphrase)
        }

        binding.passphrase.addTextChangedListener {
            binding.passLayout.error = null
        }

        viewModelFrg.working.removeObservers(viewLifecycleOwner)
        viewModelFrg.working.observe(viewLifecycleOwner, {
            getActionButton(WhichButton.POSITIVE).isEnabled = !it
        })

        viewModelFrg.passInvalid.removeObservers(viewLifecycleOwner)
        viewModelFrg.passInvalid.observe(viewLifecycleOwner,
            EventObserver {
                binding.passLayout.error =
                    getString(R.string.pass_incorect)
            })

        setCustomObservers(this)

        positiveButton {
            val password = editableToCharArray(binding.passphrase.text!!)
            onPositive(password)
            binding.passphrase.text!!.clear()
        }

        negativeButton(android.R.string.cancel) {
            dismiss()
        }
    }
}

fun SettingsFragment.shouldShowWarning() : Boolean {
    val prefWrapper = PrefWrapper.getInstance(requireContext())
    val storageType = prefWrapper.getString(KEY_STORAGE_TYPE_KEY) ?: KEY_STORAGE_SOFTWARE
    return storageType != KEY_STORAGE_HARDWARE &&
            !(prefWrapper.getBoolean(DO_NOT_SHOW_WARNING_KEY) ?: false)
}