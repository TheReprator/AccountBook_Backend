package dev.reprator.language.data

import dev.reprator.core.util.AppMapper
import dev.reprator.core.util.dbConfiguration.dbQuery
import dev.reprator.language.di.KOIN_NAMED_MAPPER_LANGUAGE
import dev.reprator.language.domain.IllegalLanguageException
import dev.reprator.language.domain.LanguageNotFoundException
import dev.reprator.language.modal.LanguageModal
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.koin.core.annotation.Property

class LanguageRepositoryImpl(@Property(KOIN_NAMED_MAPPER_LANGUAGE) private val mapper: AppMapper<ResultRow, LanguageModal>) : LanguageRepository {

    private suspend fun resultRowToLanguage(row: ResultRow): LanguageModal = mapper.map(row)

    override suspend fun allLanguage(): List<LanguageModal> = dbQuery {
        TableLanguage.selectAll().map {
            resultRowToLanguage(it)
        }.sortedBy {
            it.name
        }
    }

    override suspend fun language(id: Int): LanguageModal = dbQuery {
        TableLanguage
            .selectAll().where { TableLanguage.id eq id }
            .map {
                resultRowToLanguage(it)
            }
            .singleOrNull() ?: throw LanguageNotFoundException()
    }

    override suspend fun addNewLanguage(name: String): LanguageModal = dbQuery {
        val insertStatement = TableLanguage.insert {
            it[TableLanguage.name] = name
        }
        insertStatement.resultedValues?.singleOrNull()?.let {
            resultRowToLanguage(it)
        } ?: throw IllegalLanguageException()
    }

    override suspend fun editLanguage(id: Int, name: String): Boolean = dbQuery {
        TableLanguage.update({ TableLanguage.id eq id }) {
            it[TableLanguage.name] = name
        } > 0
    }

    override suspend fun deleteLanguage(id: Int): Boolean = dbQuery {
        TableLanguage.deleteWhere { TableLanguage.id eq id } > 0
    }
}