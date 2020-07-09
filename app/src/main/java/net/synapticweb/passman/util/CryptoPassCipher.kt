package net.synapticweb.passman.util

import android.content.Context
import android.os.Build
import android.security.KeyPairGeneratorSpec
import android.security.keystore.KeyInfo
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.synapticweb.passman.APP_TAG
import net.synapticweb.passman.ENCRYPTED_PASS_FILENAME
import java.io.*
import java.math.BigInteger
import java.security.*
import java.util.*
import javax.crypto.*
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

    override suspend fun encryptPassToDisk(passphrase: CharArray) : Boolean {
        val passBytes = charArrayToByteArray(passphrase)
        val encrypted = encrypt(passBytes)
        val path =  context.filesDir.absolutePath + "/" +
                getEncryptedFilePath()
        Arrays.fill(passBytes, 0.toByte())

        return  withContext(Dispatchers.IO) {
            try {
                val stream = FileOutputStream(path)
                stream.write(encrypted)
                stream.close()
            } catch (exc: IOException) {
                Log.e(APP_TAG, "Error writing the encrypted password: " + exc.message)
                return@withContext false
            }
            true
        }
    }

    override suspend fun decryptPassFromDisk(encFile: File): CharArray? {
        return withContext(Dispatchers.IO) {
            try {
                val reader = DataInputStream(FileInputStream(encFile))
                val nBytesToRead: Int = reader.available()
                val bytes = ByteArray(nBytesToRead)
                if (nBytesToRead > 0)
                    reader.read(bytes)

                byteArrayToCharArray(decrypt(bytes))
            } catch (exc: IOException) {
                null
            }
        }
    }
}
