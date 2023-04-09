package dev.reprator.language

import org.koin.core.KoinApplication

fun KoinApplication.setUpKoinLanguage() {
    modules(languageModule)
}
