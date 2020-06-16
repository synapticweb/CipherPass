package net.synapticweb.passman

object Authorizer {
    private var passphrase : ByteArray? = null
    private var passSetTime : Long = 0L

    fun setPassphrase(passphrase : String) {
        this.passphrase = passphrase.toByteArray()
        passSetTime = System.currentTimeMillis()
    }

    fun getPassphrase() : ByteArray? {
        return passphrase
    }

    fun timeOutExpired() : Boolean {
        return System.currentTimeMillis() - passSetTime > 30000
    }
}