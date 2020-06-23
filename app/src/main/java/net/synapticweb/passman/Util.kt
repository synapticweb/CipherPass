package net.synapticweb.passman

import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

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

//https://www.baeldung.com/java-byte-arrays-hex-strings
fun byteArrayToHexStr(bytes: ByteArray) : String {
    val builder = StringBuilder()
    for(byte in bytes) {
        val hexDigits = CharArray(2)
        hexDigits[0] = Character.forDigit((byte.toInt() shr 4) and 0xF, 16 )
        hexDigits[1] = Character.forDigit(byte.toInt() and 0xF, 16)
        builder.append(String(hexDigits))
    }
    return builder.toString()
}

fun toDigit(hexChar : Char) : Int {
    val digit = Character.digit(hexChar, 16)
    if(digit == -1) {
        throw  IllegalArgumentException("Invalid Hexadecimal Character: $hexChar")
    }
    return digit
}

fun hexStrToByteArray(inputStr : String) : ByteArray {
    val ba = ByteArray(inputStr.length / 2)
    //construct sugerat de ide. Explicat aici: https://kotlinlang.org/docs/tutorials/kotlin-for-py/loops.html
    for((baIndex, i) in (inputStr.indices step 2).withIndex()) {
        val firstDigit = toDigit(inputStr[i])
        val secondDigit = toDigit(inputStr[i + 1])
        ba[baIndex] = ((firstDigit shl 4) + secondDigit).toByte()
    }

    return ba
}

 suspend fun createHash(passphrase: String, salt : ByteArray) : ByteArray {
    val spec = PBEKeySpec(passphrase.toCharArray(), salt, 65536, 128)
    val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
    return factory.generateSecret(spec).encoded
}