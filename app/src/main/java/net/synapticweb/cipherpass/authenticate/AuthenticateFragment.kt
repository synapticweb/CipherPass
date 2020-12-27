package net.synapticweb.cipherpass.authenticate

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.databinding.BindingAdapter
import androidx.fragment.app.viewModels
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.snackbar.Snackbar
import net.synapticweb.cipherpass.*
import net.synapticweb.cipherpass.databinding.AuthenticateFragmentBinding
import net.synapticweb.cipherpass.util.*
import java.util.*
import javax.inject.Inject

const val SCROLL_HEIGHT_FACTOR = 1.1

class AuthenticateFragment : Fragment() {
    @Inject
    lateinit var viewModelFactory : ViewModelProvider.Factory

    private val _viewModel by viewModels<AuthenticateViewModel> { viewModelFactory }
    private val lockState by activityViewModels<LockStateViewModel> {viewModelFactory}

    private lateinit var binding : AuthenticateFragmentBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val parentActivity : AppCompatActivity = context as AppCompatActivity
        val app : CipherPassApp = parentActivity.application as CipherPassApp
        app.appComponent.authenticateComponent().create().inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragment = this
        binding = AuthenticateFragmentBinding.inflate(inflater, container, false).apply {
            viewModel = _viewModel
            lifecycleOwner = fragment
            val displayMetrics: DisplayMetrics = requireContext().resources.displayMetrics
            scrollHeight = (displayMetrics.heightPixels * SCROLL_HEIGHT_FACTOR).toInt()
        }
        setupSendPass()

        when(_viewModel.getApplockPref()) {
            APPLOCK_SYSTEM_VALUE -> {
                val keyMan =
                    requireActivity().getSystemService(Context.KEYGUARD_SERVICE) as
                            KeyguardManager
                val authIntent =
                    keyMan.createConfirmDeviceCredentialIntent(getString(R.string.app_name), getString(R.string.auth_subtitle))
                authIntent?.also { intent ->
                    lockState.isInUnlockActivity = true
                    startActivityForResult(intent, LOCK_ACTIVITY_CODE)
                }
                    ?: run {
                        //necesar pentru că încă nu s-a încheiat onCreateView()
                        Snackbar.make(requireActivity().findViewById(android.R.id.content), getString(R.string.system_lock_unavailable),
                            Snackbar.LENGTH_LONG).show()
                    }
            }
            APPLOCK_NOLOCK_VALUE -> {
                binding.passLayout.visibility = View.GONE
                binding.sendPass.visibility = View.GONE
                _viewModel.getPassphrase()
            }
        }

        _viewModel.passwd.observe(viewLifecycleOwner, Observer {
            if(it != null)
                _viewModel.authenticate(it)
            else
                Snackbar.make(requireActivity().findViewById(android.R.id.content), getString(R.string.system_lock_unavailable),
                    Snackbar.LENGTH_LONG).show()
        })

        setupPasswordFields(binding.passLayout, arrayOf(binding.passphrase,
            binding.passphraseRetype))

        _viewModel.authResult.observe(viewLifecycleOwner, EventObserver {
            when(it) {
                AUTH_OK -> {
                    lockState.isInUnlockActivity = false
                    binding.passphrase.text!!.clear()
                    binding.passphraseRetype.text.clear()
                    findNavController().navigate(
                        AuthenticateFragmentDirections.actionAuthenticateFragmentToEntriesListFragment()
                    )
                }

                R.string.pass_incorect ->
                    binding.passLayout.error = getString(R.string.pass_incorect)

                R.string.error_setting_pass -> Snackbar.make(requireView(),
                    getString(R.string.error_setting_pass), Snackbar.LENGTH_SHORT).show()
            }
        })

        binding.passphrase.addTextChangedListener {
            binding.passLayout.error = null
        }

        handleBackPressed(lockState)
        return binding.root
    }

    private fun setupSendPass() {
        binding.sendPass.setOnClickListener {
            if (binding.passphrase.text!!.isEmpty()) {
                binding.passLayout.error = getString(R.string.pass_empty)
                return@setOnClickListener
            }
            val pass = editableToCharArray(binding.passphrase.text!!)
            val rePass = editableToCharArray(binding.passphraseRetype.text)

            if (!_viewModel.isPassSet() && !pass.contentEquals(rePass)) {
                binding.passLayout.error = getString(R.string.pass_no_match)
                return@setOnClickListener
            }

            Arrays.fill(rePass, 0.toChar())
            binding.passphrase.text?. let {
                _viewModel.authenticate(pass)
             }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == LOCK_ACTIVITY_CODE && resultCode == Activity.RESULT_OK) {
            binding.passLayout.visibility = View.GONE
            binding.sendPass.visibility = View.GONE
            _viewModel.getPassphrase()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val appbarImage = requireActivity().findViewById<ImageView>(R.id.appbar_image)
        val ctLayout = requireActivity().findViewById<CollapsingToolbarLayout>(R.id.ct_layout)
        appbarImage?.let {
            val params = it.layoutParams
            params.height = pxFromDp(requireContext(), 200)
            it.layoutParams = params
        }

        ctLayout?.let {
            it.isTitleEnabled = true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val appbarImage = requireActivity().findViewById<ImageView>(R.id.appbar_image)
        val ctLayout = requireActivity().findViewById<CollapsingToolbarLayout>(R.id.ct_layout)
        appbarImage?.let {
            val params = it.layoutParams
            params.height = 0
            it.layoutParams = params
        }

        ctLayout?.let {
            it.isTitleEnabled = false
        }
    }
}

@BindingAdapter("bind:custom_height")
fun setLayoutHeight(view: View, height : Int) {
    val params = view.layoutParams
    params.height = height
    view.layoutParams = params
}