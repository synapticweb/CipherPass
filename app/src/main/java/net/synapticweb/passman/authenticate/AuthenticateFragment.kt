package net.synapticweb.passman.authenticate

import android.content.Context
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.fragment.app.Fragment
import net.synapticweb.passman.LockStateViewModel
import net.synapticweb.passman.PmApp
import net.synapticweb.passman.databinding.AuthenticateFragmentBinding
import net.synapticweb.passman.handleBackPressed
import javax.inject.Inject

class AuthenticateFragment : Fragment() {
    @Inject
    lateinit var viewModelFactory : ViewModelProvider.Factory

    private val viewModelFrg by viewModels<AuthenticateViewModel> { viewModelFactory }
    private val lockState by activityViewModels<LockStateViewModel> {viewModelFactory}

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val parentActivity : AppCompatActivity = context as AppCompatActivity
        val app : PmApp = parentActivity.application as PmApp
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

        val passSetListener = {
            if(lockState.unlockRepo(passphrase.text.toString()))
                findNavController().navigate(AuthenticateFragmentDirections.
                    actionAuthenticateFragmentToSecretsListFragment())
            else
                viewDataBinding.errorMessage.visibility = View.VISIBLE
        }

        val passNotSetListener = {
            if(viewModelFrg.passMatch()) {
                lockState.unlockRepo(passphrase.text.toString()) //test: totdeauna trebuie să întoarcă true
                viewModelFrg.setPassSet()
                findNavController().navigate(AuthenticateFragmentDirections.
                    actionAuthenticateFragmentToSecretsListFragment())
            }
            else
                viewDataBinding.errorPassNomatch.visibility = View.VISIBLE
        }

        viewModelFrg.passSet.observe(viewLifecycleOwner, Observer { passSet ->
            viewDataBinding.sendPass.setOnClickListener {
                if(passSet) passSetListener() else passNotSetListener()
                val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, 0)
            }
        })

        handleBackPressed(lockState)

        return viewDataBinding.root
    }
}