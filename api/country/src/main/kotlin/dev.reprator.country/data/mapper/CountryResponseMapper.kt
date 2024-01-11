package dev.reprator.country.data.mapper

import dev.reprator.core.util.AppMapper
import dev.reprator.country.data.TableCountryEntity
import dev.reprator.country.modal.CountryModal
import org.koin.core.annotation.Factory

@Factory(binds = [AppMapper::class])
class CountryResponseMapper : AppMapper<TableCountryEntity, CountryModal> {

    override suspend fun map(from: TableCountryEntity): CountryModal {
        return CountryModal.DTO(from.id.value, from.name, from.isocode, from.shortcode)
    }

}