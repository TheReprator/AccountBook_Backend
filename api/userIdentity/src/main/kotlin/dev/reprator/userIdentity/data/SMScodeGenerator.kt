package dev.reprator.userIdentity.data

import kotlin.random.Random

class SMScodeGenerator {
    fun generateCode(): Int {
        val random = Random.nextInt(999999)
        return String.format("%06d", random).toInt()
    }
}