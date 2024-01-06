package dev.reprator.core.usecase


typealias ACCESS_TOKEN = String
typealias REFRESH_TOKEN = String

interface JWTToken {
    fun generateToken(): Pair<ACCESS_TOKEN, REFRESH_TOKEN>
}