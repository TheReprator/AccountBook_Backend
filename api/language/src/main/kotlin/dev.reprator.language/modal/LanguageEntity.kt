package dev.reprator.language.modal

import dev.reprator.base.usecase.AppEntityValidator
import dev.reprator.language.domain.IllegalLanguageException

typealias LanguageId = Int
typealias LanguageName = String

interface LanguageEntity {
    val id: LanguageId
    val name: LanguageName

    data class DTO (
        override val id: LanguageId,
        override val name: LanguageName
    ) : LanguageEntity {

        companion object {
            fun Map<String, String>?.mapToModal(): DTO = object: AppEntityValidator<DTO> {

                val data = this@mapToModal ?: throw IllegalLanguageException()

                val name:String by data.withDefault { "" }

                override fun validate(): DTO {

                    if(name.isNotEmpty())
                        name.validateLanguageName()
                    else
                        throw IllegalLanguageException()

                    return DTO(0, name)
                }

            }.validate()
        }
    }
}

fun String?.validateLanguageId(): LanguageId {
    val countryCode = this?.toIntOrNull() ?: throw IllegalLanguageException()

    if(0 >= countryCode) {
        throw IllegalLanguageException()
    }

    return countryCode
}

fun String?.validateLanguageName(): LanguageName {
    val languageName = this ?: throw IllegalLanguageException()

    if(languageName.isBlank()) {
        throw IllegalLanguageException()
    }

    if(languageName.length < 3)
        throw IllegalLanguageException("Langage name can't be less than 3 character")

    return languageName
}