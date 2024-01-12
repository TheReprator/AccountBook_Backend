package dev.reprator.base.beans

const val API_HOST_IDENTIFIER = "apiHost"

enum class APIS(val value: String) {
    EXTERNAL_OTP_VERIFICATION("2FA"),
    INTERNAL_APP("myApp")
}

enum class API_BASE_URL(val value: String) {
    EXTERNAL_OTP_VERIFICATION("neutrinoapi.net"),
    INTERNAL_APP("0.0.0.0"),
}