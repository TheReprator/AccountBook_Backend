package dev.reprator.base.action

interface TokenStorage {

    val accessToken: String
    val refresh: String

    fun updateToken(accessToken: String, refresh: String)

    fun cleaToken()

    fun clearAccessToken()
}