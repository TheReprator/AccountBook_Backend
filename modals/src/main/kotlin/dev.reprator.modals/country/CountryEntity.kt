package dev.reprator.modals.country

typealias CountryId = Int
typealias CountryName = String
typealias CountryCallingCode = Int
typealias CountryShortCode = String


interface CountryEntity {
    val name: CountryName
    val callingCode: CountryCallingCode
    val shortCode: CountryShortCode
}


