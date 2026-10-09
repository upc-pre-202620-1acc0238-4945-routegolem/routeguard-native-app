package pe.edu.upc.routeguard.stakeholderassetmanagement.application

import java.security.SecureRandom

/** Automatic credentials: driver and parent accounts are provisioned with a random password. */
object TemporaryPasswordGenerator {

    private const val ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789"
    private val random = SecureRandom()

    fun generate(length: Int = 10): String =
        buildString(length) { repeat(length) { append(ALPHABET[random.nextInt(ALPHABET.length)]) } }
}
