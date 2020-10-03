package net.synapticweb.passman.credentialdetail

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import net.synapticweb.passman.CryptoPassApp
import net.synapticweb.passman.LockStateViewModel
import net.synapticweb.passman.R
import net.synapticweb.passman.databinding.CredDetailFragmentBinding
import javax.inject.Inject

class CredDetailFragment : Fragment() {
    @Inject
    lateinit var viewModelFactory : ViewModelProvider.Factory
    private val _viewModel by viewModels<CredDetailViewModel> { viewModelFactory }
    private val lockState by activityViewModels<LockStateViewModel> {viewModelFactory}
    private val args : CredDetailFragmentArgs by navArgs()
    private lateinit var binding: CredDetailFragmentBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val parentActivity : Activity = context as Activity
        val app : CryptoPassApp = parentActivity.application as CryptoPassApp
        app.appComponent.credDetailComponent().create().inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       val root = inflater.inflate(R.layout.cred_detail_fragment, container, false)
        binding = CredDetailFragmentBinding.bind(root).apply {
            viewModel = _viewModel
            fragment = this@CredDetailFragment
        }
        binding.lifecycleOwner = viewLifecycleOwner
        _viewModel.getCredential(args.credentialId)
        
        return binding.root
    }

    fun setupTogglePassword() {
        if(binding.password.transformationMethod is PasswordTransformationMethod) {
            binding.password.transformationMethod = HideReturnsTransformationMethod.getInstance()
            binding.passwordShow.setImageResource(R.drawable.eye_cut)
        }
        else {
            binding.password.transformationMethod = PasswordTransformationMethod.getInstance()
            binding.passwordShow.setImageResource(R.drawable.eye)
        }
    }

    fun fabListener(credId : Long, title : String) {
        val action = CredDetailFragmentDirections
            .actionCredDetailFragmentToAddeditCredFragment(credId, title)
        findNavController().navigate(action)
    }
}