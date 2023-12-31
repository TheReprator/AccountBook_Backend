package dev.reprator.userIdentity

import org.koin.core.KoinApplication

fun KoinApplication.setUpKoinUserIdentityModule() {
    modules(userIdentityModule)
}
