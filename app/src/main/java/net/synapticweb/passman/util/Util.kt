package net.synapticweb.passman.util

import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import net.synapticweb.passman.LockStateViewModel
import net.synapticweb.passman.R
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec


fun Fragment.handleBackPressed(lockState: LockStateViewModel) {
    requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
        val currentTime = System.currentTimeMillis()
        if(currentTime - lockState.lastBackPress > 3000) {
            lockState.lastBackPress = currentTime
            Toast.makeText(requireContext(), getText(R.string.one_more_backpress), Toast.LENGTH_SHORT)
                .show()
            return@addCallback
        }
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

fun createHash(passphrase: String, salt : ByteArray) : ByteArray {
    val spec = PBEKeySpec(passphrase.toCharArray(), salt, 65536, 128)
    val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
    return factory.generateSecret(spec).encoded
}

fun createSalt() : ByteArray {
    val random = SecureRandom()
    val salt = ByteArray(16)
    random.nextBytes(salt)
    return salt
}

@Target(
    AnnotationTarget.FUNCTION
)
@Retention(AnnotationRetention.SOURCE)
annotation class ShouldTest