package dev.reprator.dao

import dev.reprator.dao.DatabaseFactory.dbQuery
import dev.reprator.modals.Language
import dev.reprator.modals.LanguageModal
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class DAOLanguageFacadeImpl : DAOLanguageFacade {

    private fun resultRowToLanguage(row: ResultRow) = LanguageModal(
        id = row[Language.id],
        name = row[Language.name]
    )

    override suspend fun allLanguage(): List<LanguageModal> = dbQuery {
        Language.selectAll().map(::resultRowToLanguage)
    }

    override suspend fun language(id: Int): LanguageModal? = dbQuery {
        Language
            .select { Language.id eq id }
            .map(::resultRowToLanguage)
            .singleOrNull()
    }

    override suspend fun addNewLanguage(name: String): LanguageModal? = dbQuery {
        val insertStatement = Language.insert {
            it[Language.name] = name
        }
        insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToLanguage)
    }

    override suspend fun editLanguage(id: Int, name: String): Boolean = dbQuery {
        Language.update({ Language.id eq id }) {
            it[Language.name] = name
        } > 0
    }

    override suspend fun deleteLanguage(id: Int): Boolean = dbQuery {
        Language.deleteWhere { Language.id eq id } > 0
    }
}