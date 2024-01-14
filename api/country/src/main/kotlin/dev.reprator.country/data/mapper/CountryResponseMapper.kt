package dev.reprator.country.data.mapper

import dev.reprator.base.mapper.AppMapper
import dev.reprator.country.data.TableCountryEntity
import dev.reprator.modals.country.CountryModal

class CountryResponseMapper : AppMapper<TableCountryEntity, CountryModal> {

    override suspend fun map(from: TableCountryEntity): CountryModal {
        return CountryModal.DTO(from.id.value, from.name, from.isocode, from.shortcode)
    }

}