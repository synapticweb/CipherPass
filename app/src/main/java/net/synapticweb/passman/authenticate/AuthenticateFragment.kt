package net.synapticweb.passman.authenticate

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import net.synapticweb.passman.PmApp
import net.synapticweb.passman.databinding.AuthenticateFragmentBinding
import javax.inject.Inject

class AuthenticateFragment : Fragment() {
    @Inject
    lateinit var viewModelFactory : ViewModelProvider.Factory

    private val viewModelFrg by viewModels<AuthenticateViewModel> { viewModelFactory }

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
        val viewDataBinding = AuthenticateFragmentBinding.inflate(inflater, container, false).apply {
            viewModel = viewModelFrg
        }

        val sendPass = viewDataBinding.sendPass
        val pass = viewDataBinding.passphrase
        sendPass.setOnClickListener {
            if(viewModelFrg.unlockRepo(pass.text.toString())) {
                findNavController().navigate(AuthenticateFragmentDirections.
                    actionAuthenticateFragmentToSecretsListFragment())
                viewModelFrg.setPassSet()
            }
            else
                viewDataBinding.errorMessage.visibility = View.VISIBLE
        }

        return viewDataBinding.root
    }
}