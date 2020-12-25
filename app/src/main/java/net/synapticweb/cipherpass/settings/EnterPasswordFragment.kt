package net.synapticweb.cipherpass.settings

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import net.synapticweb.cipherpass.CipherPassApp
import net.synapticweb.cipherpass.R
import net.synapticweb.cipherpass.databinding.EnterPasswordFragmentBinding
import net.synapticweb.cipherpass.util.editableToCharArray

import javax.inject.Inject

class EnterPasswordFragment : Fragment() {
    @Inject
    lateinit var viewModelFactory : ViewModelProvider.Factory
    private val viewModelFrg by viewModels<EnterPasswordViewModel> {viewModelFactory}

    private val args : EnterPasswordFragmentArgs by navArgs()

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
        val fragment = this
        viewModelFrg.prefValue = args.prefEntryValue

        val viewDataBinding = EnterPasswordFragmentBinding.inflate(inflater, container, false).apply {
            viewModel = viewModelFrg
            lifecycleOwner = fragment
            arguments = args
        }

        viewModelFrg.errorPassNoMatch.observe(viewLifecycleOwner, Observer {
            if(it)
                viewDataBinding.passLayout.error = getString(R.string.pass_incorect)
        })

        viewModelFrg.storageSoft.observe(viewLifecycleOwner, Observer {
            viewDataBinding.cancelButton.visibility = View.VISIBLE
            viewDataBinding.softStorageWarning.visibility = View.VISIBLE
            viewDataBinding.cancelButton.setOnClickListener {
                viewModelFrg.onStorageSoftRenounce()
            }
            viewDataBinding.actionButton.text = getString(android.R.string.yes)
            viewDataBinding.passLayout.visibility = View.GONE
            viewDataBinding.actionButton.setOnClickListener {
                viewModelFrg.onStorageSoftAccept()
            }
        })

        viewModelFrg.errorFileWriteFail.observe(viewLifecycleOwner, Observer {
            Snackbar.make(requireView(), getString(R.string.system_lock_file_write_fail),
                Snackbar.LENGTH_SHORT).show()
        })

        viewModelFrg.finish.observe(viewLifecycleOwner, Observer {
            findNavController().
            navigate(EnterPasswordFragmentDirections.actionSystemLockFragmentToSettingsFragment())
        })

        viewDataBinding.passphrase.addTextChangedListener {
            viewDataBinding.passLayout.error = null
        }

        viewDataBinding.actionButton.setOnClickListener {
            if(viewDataBinding.passphrase.text?.length == 0){
                viewDataBinding.passLayout.error = getString(R.string.pass_empty)
                return@setOnClickListener
            }
            viewDataBinding.passphrase.text?.let {
                //char array-ul rezultat e șters în encryptPass()
                viewModelFrg.validatePass(editableToCharArray(it))
                it.clear()
            }
        }

        return viewDataBinding.root
    }
}