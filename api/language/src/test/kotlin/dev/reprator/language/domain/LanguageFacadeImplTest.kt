package dev.reprator.language.domain

import dev.reprator.language.modal.LanguageEntity
import dev.reprator.language.modal.LanguageId
import dev.reprator.language.modal.LanguageModal
import dev.reprator.language.modal.LanguageName

class LanguageFacadeImplTest : LanguageFacade {

    companion object {
        val LANGUAGE_LIST = mutableListOf<LanguageModal>()
    }

    override suspend fun getAllLanguage(): Iterable<LanguageModal> {
        return LANGUAGE_LIST
    }

    override suspend fun getLanguage(id: LanguageId): LanguageModal {
        return LANGUAGE_LIST.find {
            it.id == id
        } ?: throw LanguageNotFoundException()
    }

    override suspend fun addNewLanguage(languageInfo: LanguageName): LanguageModal {
        val languageExistenceCheck = LANGUAGE_LIST.find {
            it.name.equals(languageInfo, true)
        }

        if (null != languageExistenceCheck) {
            throw IllegalLanguageException()
        }

        val newLanguageModal = LanguageModal.DTO(3, languageInfo)
        LANGUAGE_LIST.add(newLanguageModal)

        return newLanguageModal
    }

    override suspend fun editLanguage(languageId: LanguageId, languageInfo: LanguageEntity): Boolean {
        val languageIndex = LANGUAGE_LIST.indexOfFirst {
            it.id == languageInfo.id
        }

        if (-1 == languageIndex) {
            throw IllegalLanguageException()
        }

        val updatedLanguageModal = LanguageModal.DTO(languageInfo.id, languageInfo.name)
        LANGUAGE_LIST[languageIndex] = updatedLanguageModal
        return true
    }

    override suspend fun deleteLanguage(id: LanguageId): Boolean {
        val languageIndex = LANGUAGE_LIST.indexOfFirst {
            it.id == id
        }

        if (-1 == languageIndex) {
            throw IllegalLanguageException()
        }

        LANGUAGE_LIST.removeAt(languageIndex)
        return true
    }
}