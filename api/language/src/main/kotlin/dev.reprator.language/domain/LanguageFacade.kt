package dev.reprator.language.domain

import dev.reprator.language.modal.LanguageEntity
import dev.reprator.language.modal.LanguageId
import dev.reprator.language.modal.LanguageModal
import dev.reprator.language.modal.LanguageName

interface LanguageFacade {

    @Throws(LanguageEmptyException::class)
    suspend fun getAllLanguage(): Iterable<LanguageModal>

    suspend fun getLanguage(id: LanguageId): LanguageModal

    suspend fun addNewLanguage(languageInfo: LanguageName): LanguageModal

    suspend fun editLanguage(languageId: LanguageId, languageInfo: LanguageEntity): Boolean

    suspend fun deleteLanguage(id: LanguageId): Boolean

}