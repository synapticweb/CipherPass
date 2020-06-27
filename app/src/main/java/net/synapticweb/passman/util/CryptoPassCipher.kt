package net.synapticweb.passman.util

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyInfo
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.annotation.RequiresApi
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec

const val SYM_KEY_ALIAS : String = "cryptopass"
const val SYM_TRANSFORMATION = "AES/GCM/NoPadding"
const val IV_SEPARATOR = "]"
const val SYM_PROVIDER = "AndroidKeyStore"


//Criptare simetrică: https://medium.com/@josiassena/using-the-android-keystore-system-to-store-sensitive-information-3a56175a454b
//Asimetrică, posibil pe Android 5: https://www.androidauthority.com/use-android-keystore-store-passwords-sensitive-information-623779/
//https://doridori.github.io/android-security-the-forgetful-keystore/#sthash.2CIutzAZ.dpbs
//https://source.android.com/security/keystore
//IV + o serie de articole despre criptare în Android:
//https://proandroiddev.com/secure-data-in-android-initialization-vector-6ca1c659762c
@RequiresApi(Build.VERSION_CODES.M)
class CryptoPassCipher {
    companion object {
        private var keyStore: KeyStore = KeyStore.getInstance(SYM_PROVIDER)

        init {
            keyStore.load(null)
        }


        private fun getSecretKey(): SecretKey {
            if (!keyStore.containsAlias(
                    SYM_KEY_ALIAS
                ))
                createSecretKey()
            return (keyStore.getEntry(
                SYM_KEY_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
        }

        fun isStorageHardwareBacked(): Boolean {
            val key =
                getSecretKey()
            val keyFactory = SecretKeyFactory.getInstance(key.algorithm,
                SYM_PROVIDER
            )
            val keyInfo = keyFactory.getKeySpec(key, KeyInfo::class.java) as KeyInfo
            return keyInfo.isInsideSecureHardware
        }

        private fun createSecretKey() {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,
                SYM_PROVIDER
            )
            keyGenerator.init(
                KeyGenParameterSpec.Builder(
                    SYM_KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build()
            )

            keyGenerator.generateKey()
        }

        fun encrypt(strToEncrypt: String): String {
            val cipher = Cipher.getInstance(SYM_TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE,
                getSecretKey()
            )
            val ivString = Base64.encodeToString(cipher.iv, Base64.DEFAULT)

            val bytes = cipher.doFinal(strToEncrypt.toByteArray())
            var result = Base64.encodeToString(bytes, Base64.DEFAULT)
            result += (IV_SEPARATOR + ivString)

            return result
        }

        fun decrypt(strToDecrypt: String): String {
            val split = strToDecrypt.split(IV_SEPARATOR.toRegex())
            val encodedString = split[0]
            val ivString = split[1]
            val cipher = Cipher.getInstance(SYM_TRANSFORMATION)

            val spec = GCMParameterSpec(128, Base64.decode(ivString, Base64.DEFAULT))
            cipher.init(Cipher.DECRYPT_MODE,
                getSecretKey(), spec)

            val bytes = Base64.decode(encodedString, Base64.DEFAULT)
            return String(cipher.doFinal(bytes))
        }
    }
}
