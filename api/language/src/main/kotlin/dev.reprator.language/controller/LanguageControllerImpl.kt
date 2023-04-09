package dev.reprator.language.controller

import dev.reprator.language.domain.LanguageFacade
import dev.reprator.language.modal.LanguageEntity
import dev.reprator.language.modal.LanguageId
import dev.reprator.language.modal.LanguageModal
import dev.reprator.language.modal.LanguageName

class LanguageControllerImpl(private val languageFacade: LanguageFacade) : LanguageController {

    override suspend fun getAllLanguage(): Iterable<LanguageModal> {
        return languageFacade.getAllLanguage()
    }

    override suspend fun getLanguage(id: LanguageId): LanguageModal {
        return languageFacade.getLanguage(id)
    }

    override suspend fun addNewLanguage(languageInfo: LanguageName): LanguageModal {
        return languageFacade.addNewLanguage(languageInfo)
    }

    override suspend fun editLanguage(languageId: LanguageId, languageInfo: LanguageEntity): Boolean {
        return languageFacade.editLanguage(languageId, languageInfo)
    }

    override suspend fun deleteLanguage(id: LanguageId): Boolean {
        return languageFacade.deleteLanguage(id)
    }

}

