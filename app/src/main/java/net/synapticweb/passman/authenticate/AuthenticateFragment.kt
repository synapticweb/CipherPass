package net.synapticweb.passman.authenticate

import android.content.Context
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.fragment.app.Fragment
import net.synapticweb.passman.*
import net.synapticweb.passman.databinding.AuthenticateFragmentBinding
import net.synapticweb.passman.util.EventObserver
import net.synapticweb.passman.util.handleBackPressed
import javax.inject.Inject

class AuthenticateFragment : Fragment() {
    @Inject
    lateinit var viewModelFactory : ViewModelProvider.Factory

    private val viewModelFrg by viewModels<AuthenticateViewModel> { viewModelFactory }
    private val lockState by activityViewModels<LockStateViewModel> {viewModelFactory}

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val parentActivity : AppCompatActivity = context as AppCompatActivity
        val app : CryptoPassApp = parentActivity.application as CryptoPassApp
        app.appComponent.authenticateComponent().create().inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragment = this
        val viewDataBinding = AuthenticateFragmentBinding.inflate(inflater, container, false).apply {
            viewModel = viewModelFrg
            lifecycleOwner = fragment
            lockStateViewModel = lockState
        }

        val passphrase = viewDataBinding.passphrase
        val rePassphrase = viewDataBinding.passphraseRetype

        //https://medium.com/@droidbyme/show-hide-password-in-edittext-in-android-c4c3db44f734
        viewDataBinding.passLayout.setEndIconOnClickListener {
            if(passphrase.transformationMethod is PasswordTransformationMethod) {
                passphrase.transformationMethod = HideReturnsTransformationMethod.getInstance()
                rePassphrase.transformationMethod = HideReturnsTransformationMethod.getInstance()
            }
            else {
                passphrase.transformationMethod = PasswordTransformationMethod.getInstance()
                rePassphrase.transformationMethod = PasswordTransformationMethod.getInstance()
            }
        }

        viewDataBinding.sendPass.setOnClickListener {
            if(viewModelFrg.passEmpty()) {
                viewDataBinding.passLayout.error = getString(R.string.pass_empty)
                return@setOnClickListener
            }
            if(!viewModelFrg.isPassSet() && !viewModelFrg.passMatch()) {
                viewDataBinding.passLayout.error = getString(R.string.pass_no_match)
                return@setOnClickListener
            }
            lockState.unlockRepo(passphrase.text.toString())
        }

        lockState.unlockSuccess.observe(viewLifecycleOwner,
            EventObserver {
                if (it) {
                    if (!viewModelFrg.isPassSet()) {
                        viewModelFrg.setPassSet()
                        viewModelFrg.createPassHash(passphrase.text.toString())
                    }
                    findNavController().navigate(
                        AuthenticateFragmentDirections.actionAuthenticateFragmentToSecretsListFragment()
                    )
                } else
                    viewDataBinding.passLayout.error = getString(R.string.pass_incorect)
            })

        passphrase.addTextChangedListener {
            viewDataBinding.passLayout.error = null
        }

        handleBackPressed(lockState)
        return viewDataBinding.root
    }
}