package net.synapticweb.passman.credentialdetail

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.afollestad.materialdialogs.MaterialDialog
import net.synapticweb.passman.CryptoPassApp
import net.synapticweb.passman.LockStateViewModel
import net.synapticweb.passman.R
import net.synapticweb.passman.databinding.CredDetailFragmentBinding
import net.synapticweb.passman.util.EventObserver
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
        setHasOptionsMenu(true)

        _viewModel.finishDeletion.observe(viewLifecycleOwner, EventObserver {
            Toast.makeText(requireContext(), getString(R.string.deletion_ok), Toast.LENGTH_SHORT).show()
            val action = CredDetailFragmentDirections.actionCredDetailFragmentToCredListFragment()
            findNavController().navigate(action)
        })

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.detail_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.delete -> {
                MaterialDialog(requireContext()).show {
                    title(R.string.confirm_deletion_title)
                    message(R.string.confirm_deletion_message)
                    positiveButton(android.R.string.ok) {
                        _viewModel.delete()
                    }
                    negativeButton(android.R.string.cancel) {  }
                }
                true
            }
            else -> false
        }
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