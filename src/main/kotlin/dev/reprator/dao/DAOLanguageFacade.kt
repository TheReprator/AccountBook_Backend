package dev.reprator.dao

import dev.reprator.modals.LanguageModal

interface DAOLanguageFacade {
    suspend fun allLanguage(): List<LanguageModal>
    suspend fun language(id: Int): LanguageModal?
    suspend fun addNewLanguage(name: String): LanguageModal?
    suspend fun editLanguage(id: Int, name: String): Boolean
    suspend fun deleteLanguage(id: Int): Boolean
}