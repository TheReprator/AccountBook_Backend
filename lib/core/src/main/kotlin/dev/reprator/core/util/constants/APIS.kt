package dev.reprator.core.util.constants

enum class APIS(val value: String) {
    EXTERNAL_OTP_VERIFICATION("2FA"),
    INTERNAL_APP("myApp")
}

enum class API_BASE_URL(val value: String) {
    EXTERNAL_OTP_VERIFICATION("https://neutrinoapi.net"),
    INTERNAL_APP("http://0.0.0.0"),
}