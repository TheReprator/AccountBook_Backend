package dev.reprator.country.data.mapper

import dev.reprator.core.Mapper
import dev.reprator.country.data.TableCountryEntity
import dev.reprator.country.modal.CountryModal

class CountryResponseMapper : Mapper<TableCountryEntity, CountryModal> {

    override suspend fun map(from: TableCountryEntity): CountryModal {
        return CountryModal.DTO(from.id.value, from.name, from.isocode, from.shortcode)
    }

}