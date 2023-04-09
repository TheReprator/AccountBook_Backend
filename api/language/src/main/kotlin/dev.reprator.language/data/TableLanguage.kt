package dev.reprator.language.data

import org.jetbrains.exposed.sql.*

object TableLanguage : Table(name="language") {
    val id = integer("id").autoIncrement()
    val name = text("name")

    override val primaryKey = PrimaryKey(id)
}