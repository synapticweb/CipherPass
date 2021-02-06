/*
 * Copyright (C) 2021 Eugen Rădulescu <synapticwebb@gmail.com>
 * This file is part of the CipherPass project ( https://github.com/synapticweb/CipherPass ).
 * See the LICENSE file in the project root for license terms.
 */

package net.synapticweb.cipherpass.util

import android.content.Context
import android.text.Editable
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.DisplayMetrics
import android.widget.EditText
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.getActionButton
import com.google.android.material.textfield.TextInputLayout
import net.synapticweb.cipherpass.LockStateViewModel
import net.synapticweb.cipherpass.R
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.Charset
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

const val SET_ICON_COLUMN_WIDTH = 55

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

//https://www.baeldung.com/java-password-hashing
fun createHashPBKDF2(passphrase: CharArray, salt : ByteArray) : ByteArray {
    val spec = PBEKeySpec(passphrase, salt, 65536, 128)
    val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
    return factory.generateSecret(spec).encoded
}

fun createHashMd5(passphrase: ByteArray, salt : ByteArray) : ByteArray {
    val md: MessageDigest = MessageDigest.getInstance("MD5")
    md.update(salt)
    return md.digest(passphrase)
}

fun createHashSha(passphrase: ByteArray, salt : ByteArray) : ByteArray {
    val md: MessageDigest = MessageDigest.getInstance("SHA-512")
    md.update(salt)
    return md.digest(passphrase)
}

fun createSalt() : ByteArray {
    val random = SecureRandom()
    val salt = ByteArray(16)
    random.nextBytes(salt)
    return salt
}

//https://stackoverflow.com/a/9670279
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
    return bytes
}

fun byteArrayToCharArray(bytes : ByteArray) : CharArray {
    val byteBuffer = ByteBuffer.wrap(bytes)
    val charBuffer = Charset.forName("UTF-8").decode(byteBuffer)
    val chars = Arrays.copyOfRange(charBuffer.array(), charBuffer.position(), charBuffer.limit())
    Arrays.fill(charBuffer.array(), 0.toChar())

    return chars
}

//https://medium.com/@droidbyme/show-hide-password-in-edittext-in-android-c4c3db44f734
fun setupPasswordFields(layout : TextInputLayout,
                        inputs : Array<EditText>) {
    layout.setEndIconOnClickListener {
        if (inputs[0].transformationMethod is PasswordTransformationMethod) {
            for(element in inputs) {
                element.transformationMethod = HideReturnsTransformationMethod.getInstance()
            }
        } else {
            for(element in inputs) {
                element.transformationMethod = PasswordTransformationMethod.getInstance()
            }
        }
    }

}

fun getSetIconNumColumns(context : Context) : Int {
    val displayMetrics: DisplayMetrics = context.resources.displayMetrics
    val screenWidthDp = displayMetrics.widthPixels / displayMetrics.density
    return (screenWidthDp / SET_ICON_COLUMN_WIDTH + 0.5).toInt()
}

fun pxFromDp(context: Context, dp: Int): Int {
    return (dp * context.resources.displayMetrics.density).toInt()
}

fun dpFromPx(context: Context, px : Int) : Int {
    return (px / context.resources.displayMetrics.density).toInt()
}

fun disablePositiveWhenEmpty(dialog : MaterialDialog, editTextId : Int) {
    val positive = dialog.getActionButton(WhichButton.POSITIVE)
    positive.isEnabled = false
    val editText = dialog.findViewById<EditText>(editTextId)
    editText.addTextChangedListener(
        afterTextChanged = { editable ->
            if (editable != null) {
                positive.isEnabled = editable.isNotEmpty()
            }
        })
}