package net.synapticweb.passman

import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment


fun Fragment.handleBackPressed(lockState: LockStateViewModel) {
    requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
        if(!lockState.pressedOnce) {
            lockState.pressedOnce = true
            Toast.makeText(requireContext(), getText(R.string.one_more_backpress), Toast.LENGTH_SHORT)
                .show()
            return@addCallback
        }
        lockState.pressedOnce = false
        lockState.lockRepo()
        requireActivity().finish()
    }
}
