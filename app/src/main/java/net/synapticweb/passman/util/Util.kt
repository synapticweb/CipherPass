package net.synapticweb.passman.util

import android.text.Editable
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import net.synapticweb.passman.LockStateViewModel
import net.synapticweb.passman.R
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.Charset
import java.security.SecureRandom
import java.util.*
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

fun createHash(passphrase: CharArray, salt : ByteArray, erasePassphrase : Boolean) : ByteArray {
    val spec = PBEKeySpec(passphrase, salt, 65536, 128)
    val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
    val hash = factory.generateSecret(spec).encoded
    if(erasePassphrase)
        Arrays.fill(passphrase, 0.toChar())
    return hash
}

fun createSalt() : ByteArray {
    val random = SecureRandom()
    val salt = ByteArray(16)
    random.nextBytes(salt)
    return salt
}

//https://stackoverflow.com/a/9670279
fun editableToByteArray(input : Editable): ByteArray {
    val chars = CharArray(input.length)
    input.getChars(0, input.length, chars, 0)

    val charBuffer: CharBuffer = CharBuffer.wrap(chars)
    val byteBuffer: ByteBuffer = Charset.forName("UTF-8").encode(charBuffer)
    val bytes: ByteArray = Arrays.copyOfRange(
        byteBuffer.array(),
        byteBuffer.position(), byteBuffer.limit()
    )
    Arrays.fill(byteBuffer.array(), 0.toByte())
    Arrays.fill(chars, 0.toChar())
    return bytes
}

fun editableToCharArray(input : Editable) : CharArray {
    val chars = CharArray(input.length)
    input.getChars(0, input.length, chars, 0)
    return chars
}

fun charArrayToByteArray(chars : CharArray) : ByteArray {
    val charBuffer: CharBuffer = CharBuffer.wrap(chars)
    val byteBuffer: ByteBuffer = Charset.forName("UTF-8").encode(charBuffer)
    val bytes: ByteArray = Arrays.copyOfRange(
        byteBuffer.array(),
        byteBuffer.position(), byteBuffer.limit()
    )
    Arrays.fill(byteBuffer.array(), 0.toByte())
    Arrays.fill(chars, 0.toChar()) //de văzut dacă e bine să șterg
    return bytes
}

@Target(
    AnnotationTarget.FUNCTION
)
@Retention(AnnotationRetention.SOURCE)
annotation class ShouldTest