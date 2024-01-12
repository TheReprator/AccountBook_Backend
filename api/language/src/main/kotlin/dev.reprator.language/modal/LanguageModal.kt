package dev.reprator.language.modal

interface LanguageModal : LanguageEntity {

    data class DTO (
        override val id: LanguageId,
        override val name: LanguageName
    ) : LanguageModal
}