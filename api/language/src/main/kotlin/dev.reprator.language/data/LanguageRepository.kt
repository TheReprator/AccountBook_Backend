package dev.reprator.language.data

import dev.reprator.language.modal.LanguageModal

interface LanguageRepository {

    suspend fun allLanguage(): List<LanguageModal>

    suspend fun language(id: Int): LanguageModal

    suspend fun addNewLanguage(name: String): LanguageModal

    suspend fun editLanguage(id: Int, name: String): Boolean

    suspend fun deleteLanguage(id: Int): Boolean

}