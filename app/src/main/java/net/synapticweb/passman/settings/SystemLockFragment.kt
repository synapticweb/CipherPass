package net.synapticweb.passman.settings

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
import com.google.android.material.snackbar.Snackbar
import net.synapticweb.passman.CryptoPassApp
import net.synapticweb.passman.R
import net.synapticweb.passman.databinding.SystemLockFragmentBinding

import javax.inject.Inject

class SystemLockFragment : Fragment() {
    @Inject
    lateinit var viewModelFactory : ViewModelProvider.Factory
    private val viewModelFrg by viewModels<SystemLockViewModel> {viewModelFactory}

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
        val fragment = this
        val viewDataBinding = SystemLockFragmentBinding.inflate(inflater, container, false).apply {
            viewModel = viewModelFrg
            lifecycleOwner = fragment
        }


        viewModelFrg.errorPassNoMatch.observe(viewLifecycleOwner, Observer {
            if(it)
                viewDataBinding.passLayout.error = getString(R.string.pass_incorect)
        })

        viewModelFrg.storageSoft.observe(viewLifecycleOwner, Observer {
            viewDataBinding.cancelButton.visibility = View.VISIBLE
            viewDataBinding.softStorageWarning.visibility = View.VISIBLE
            viewDataBinding.cancelButton.setOnClickListener {
                findNavController().
                navigate(SystemLockFragmentDirections.actionSystemLockFragmentToSettingsFragment())
            }
            viewDataBinding.actionButton.text = getString(android.R.string.yes)
            viewDataBinding.passLayout.visibility = View.GONE
            viewDataBinding.actionButton.setOnClickListener {
                viewModelFrg.encryptPassAndSetPref()
            }
        })

        viewModelFrg.errorFileWriteFail.observe(viewLifecycleOwner, Observer {
            Snackbar.make(requireView(), getString(R.string.system_lock_file_write_fail),
                Snackbar.LENGTH_SHORT).show()
        })

        viewModelFrg.finish.observe(viewLifecycleOwner, Observer {
            findNavController().
            navigate(SystemLockFragmentDirections.actionSystemLockFragmentToSettingsFragment())
        })

        viewDataBinding.passphrase.addTextChangedListener {
            viewDataBinding.passLayout.error = null
        }

        viewDataBinding.actionButton.setOnClickListener {
            if(viewDataBinding.passphrase.text?.length == 0){
                viewDataBinding.passLayout.error = getString(R.string.pass_empty)
                return@setOnClickListener
            }
            viewModelFrg.validatePass(viewDataBinding.passphrase.text.toString())
        }

        return viewDataBinding.root
    }
}