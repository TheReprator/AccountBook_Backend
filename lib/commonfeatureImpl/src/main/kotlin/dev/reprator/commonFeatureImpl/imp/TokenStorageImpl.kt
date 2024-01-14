package dev.reprator.commonFeatureImpl.imp

import dev.reprator.base.action.TokenStorage

class TokenStorageImpl : TokenStorage {

    override var accessToken: String= ""
    override var refresh: String = ""

    override fun updateToken(accessToken: String, refresh: String) {
        this.accessToken = accessToken
        this.refresh = refresh
    }

    override fun cleaToken() {
        accessToken = ""
        refresh = ""
    }

    override fun clearAccessToken() {
        accessToken = ""
    }
}