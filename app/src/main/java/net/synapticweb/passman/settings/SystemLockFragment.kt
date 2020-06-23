package net.synapticweb.passman.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
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
        val parentActivity : AppCompatActivity = context as AppCompatActivity
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

        val actionButton = viewDataBinding.root.findViewById<Button>(R.id.action_button)
        val passwd = viewDataBinding.root.findViewById<TextInputEditText>(R.id.passphrase)
        val layoutPasswd = viewDataBinding.root.findViewById<TextInputLayout>(R.id.pass_layout)

        viewModelFrg.errorPassNoMatch.observe(viewLifecycleOwner, Observer {
            if(it)
                layoutPasswd.error = getString(R.string.pass_incorect)
        })

        viewModelFrg.storageSoft.observe(viewLifecycleOwner, Observer {
            val cancelButton = viewDataBinding.root.findViewById<Button>(R.id.cancel_button)
            val warning = viewDataBinding.root.findViewById<TextView>(R.id.soft_storage_warning)
            cancelButton.visibility = View.VISIBLE
            warning.visibility = View.VISIBLE
            cancelButton.setOnClickListener {
                findNavController().
                navigate(SystemLockFragmentDirections.actionSystemLockFragmentToSettingsFragment())
            }
            actionButton.text = getString(android.R.string.yes)
            layoutPasswd.visibility = View.GONE
            actionButton.setOnClickListener {
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

        passwd.addTextChangedListener {
            layoutPasswd.error = null
        }

        actionButton.setOnClickListener {
            if(passwd.text?.length == 0) return@setOnClickListener
            viewModelFrg.validatePass(passwd.text.toString())
        }

        return viewDataBinding.root
    }
}