package dev.reprator.country.data

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

class TableCountryEntity(id: EntityID<Int>) : IntEntity(id) {
    var name by TableCountry.name
    var isocode by TableCountry.callingCode
    var shortcode by TableCountry.shortcode

    companion object : IntEntityClass<TableCountryEntity>(TableCountry)
}

object TableCountry : IntIdTable("country", "id") {
    val name = text("name").uniqueIndex()
    val callingCode = integer("callingcode").uniqueIndex()
    val shortcode = text("shortcode").uniqueIndex()

    init {
        uniqueIndex(name, callingCode, shortcode)
    }
}