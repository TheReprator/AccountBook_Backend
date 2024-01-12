package dev.reprator.language.data.mapper

import dev.reprator.base.mapper.AppMapper
import dev.reprator.language.data.TableLanguage
import dev.reprator.language.modal.LanguageModal
import org.jetbrains.exposed.sql.ResultRow

class LanguageResponseMapper : AppMapper<ResultRow, LanguageModal> {

    override suspend fun map(from: ResultRow): LanguageModal {
        return LanguageModal.DTO(from[TableLanguage.id], from[TableLanguage.name])
    }

}