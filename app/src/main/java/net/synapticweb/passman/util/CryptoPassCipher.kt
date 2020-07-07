package net.synapticweb.passman.util

import android.content.Context
import android.os.Build
import android.security.KeyPairGeneratorSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyInfo
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.annotation.RequiresApi
import net.synapticweb.passman.ENCRYPTED_PASS_FILENAME
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.math.BigInteger
import java.nio.charset.Charset
import java.security.*
import java.util.*
import javax.crypto.*
import javax.crypto.spec.GCMParameterSpec
import javax.security.auth.x500.X500Principal

const val ASYM_KEY_ALIAS : String = "passkey"
const val ASYM_ALGORITHM : String = "RSA"
const val ASYM_TRANSFORMATION = "RSA/ECB/PKCS1Padding"
const val ASYM_PROVIDER = "AndroidKeyStore"


//Criptare simetrică: https://medium.com/@josiassena/using-the-android-keystore-system-to-store-sensitive-information-3a56175a454b
//Asimetrică, posibil pe Android 5: https://www.androidauthority.com/use-android-keystore-store-passwords-sensitive-information-623779/
//https://doridori.github.io/android-security-the-forgetful-keystore/#sthash.2CIutzAZ.dpbs
//https://source.android.com/security/keystore
//IV + o serie de articole despre criptare în Android:
//https://proandroiddev.com/secure-data-in-android-initialization-vector-6ca1c659762c
open class CryptoPassCipher(private val context : Context) : CPCipher {
        private var keyStore: KeyStore = KeyStore.getInstance(ASYM_PROVIDER)

        init {
            keyStore.load(null)
        }

    private fun createKeyPair() {
        val keyGenerator = KeyPairGenerator.getInstance(ASYM_ALGORITHM, ASYM_PROVIDER)
        val start = Calendar.getInstance()
        val end = Calendar.getInstance()
        end.add(Calendar.YEAR, 1)
        val spec = KeyPairGeneratorSpec.Builder(context)
            .setAlias(ASYM_KEY_ALIAS)
            .setSubject(X500Principal("CN=Sample Name, O=Android Authority"))
            .setSerialNumber(BigInteger.ONE)
            .setStartDate(start.time)
            .setEndDate(end.time)
            .build()

        keyGenerator.initialize(spec)
        keyGenerator.generateKeyPair()
    }

    private fun getPublicKey() : PublicKey {
        if(!keyStore.containsAlias(ASYM_KEY_ALIAS))
            createKeyPair()

        return (keyStore.getEntry(ASYM_KEY_ALIAS, null) as KeyStore.PrivateKeyEntry).certificate.publicKey
    }

    private fun getPrivateKey() : PrivateKey {
        if(!keyStore.containsAlias(ASYM_KEY_ALIAS))
            createKeyPair()
        return (keyStore.getEntry(ASYM_KEY_ALIAS, null) as KeyStore.PrivateKeyEntry).privateKey
    }

    override fun isStorageHardwareBacked(): Boolean {
        if(Build.VERSION.SDK_INT < 23)
            return false
        val key = getPrivateKey()
        val keyFactory = KeyFactory.getInstance(key.algorithm, ASYM_PROVIDER)
        val keyInfo = keyFactory.getKeySpec(key, KeyInfo::class.java)

        return keyInfo.isInsideSecureHardware
    }

    override fun decrypt(input : ByteArray) : ByteArray {
        val cipher = Cipher.getInstance(ASYM_TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, getPrivateKey())
        val cipherInputStream = CipherInputStream(ByteArrayInputStream(input), cipher)
        val values = arrayListOf<Byte>()
        var nextByte : Int

        while (cipherInputStream.read().also { nextByte = it } != -1) {
            values.add(nextByte.toByte())
        }

        val bytes = ByteArray(values.size)
        for (i in bytes.indices) {
            bytes[i] = values[i]
        }

        return bytes
    }

    override fun encrypt(input : ByteArray) : ByteArray {
        val cipher = Cipher.getInstance(ASYM_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getPublicKey())
        val outputStream = ByteArrayOutputStream()
        val cipherOutputStream = CipherOutputStream(outputStream, cipher)
        cipherOutputStream.write(input)
        cipherOutputStream.close()

        return outputStream.toByteArray()
    }

    override fun getEncryptedFilePath(): String = ENCRYPTED_PASS_FILENAME
}
