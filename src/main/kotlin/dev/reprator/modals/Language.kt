package dev.reprator.modals

import org.jetbrains.exposed.sql.*
import java.io.Serializable

data class LanguageModal(val id: Int, val name: String): Serializable

object Language : Table() {
    val id = integer("id").autoIncrement()
    val name = text("name")

    override val primaryKey = PrimaryKey(id)
}