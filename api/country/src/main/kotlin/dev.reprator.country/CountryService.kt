package dev.reprator.country

import org.koin.core.KoinApplication

fun KoinApplication.setUpKoinCountry() {
    modules(countryModule)
}
