package src.utils

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

class SecUtils {
    var encoded: String? = null
    fun GeneratePrivateKey(text: String): String? {
        try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(text.toByteArray(StandardCharsets.UTF_8))
            encoded = Base64.getEncoder().encodeToString(hash)
        } catch (e: NoSuchAlgorithmException) {
            println("error :$e")
        }
        return encoded
    }

    fun GenerateHash(key: String?): String? {
        val hash1 = GenerateHashH(key)
        return GenerateHashH(hash1)
    }

    private fun GenerateHashH(key: String?): String? {
        // key == address + created at + sender + receiver+ private key hash of the block
        try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(key!!.toByteArray(StandardCharsets.UTF_8))
            encoded = Base64.getEncoder().encodeToString(hash)
            encoded = encoded!!.replace("/", "7")
            encoded = encoded!!.replace("\\\\", "y")
        } catch (e: NoSuchAlgorithmException) {
            println("error :$e")
        }
        return encoded
    }

    fun GenerateRandString(): String {
        var wfName = ""
        val r = Random()
        for (i in 0..31) {
            val randomChar = (97 + r.nextInt(26)).toChar()
            wfName = wfName + randomChar
        }
        println(wfName)
        return wfName
    }
}