package dev.reprator.language.controller

import dev.reprator.language.domain.IllegalLanguageException
import dev.reprator.language.domain.LanguageEmptyException
import dev.reprator.language.domain.LanguageNotFoundException
import dev.reprator.language.modal.LanguageEntity
import dev.reprator.language.modal.LanguageId
import dev.reprator.language.modal.LanguageModal
import dev.reprator.language.modal.LanguageName


interface LanguageController  {

    @Throws(LanguageEmptyException::class)
    suspend fun getAllLanguage(): Iterable<LanguageModal>

    @Throws(LanguageNotFoundException::class)
    suspend fun getLanguage(id: LanguageId): LanguageModal

    @Throws(IllegalLanguageException::class)
    suspend fun addNewLanguage(languageInfo: LanguageName): LanguageModal

    @Throws(IllegalLanguageException::class)
    suspend fun editLanguage(languageId: LanguageId, languageInfo: LanguageEntity): Boolean

    @Throws(IllegalLanguageException::class)
    suspend fun deleteLanguage(id: LanguageId): Boolean
}

