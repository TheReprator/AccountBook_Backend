package dev.reprator.country.controller

import dev.reprator.base_ktor.util.respondWithResult
import dev.reprator.country.domain.IllegalCountryException
import dev.reprator.country.modal.CountryEntityDTO
import dev.reprator.country.modal.CountryEntityDTO.Companion.mapToModal
import dev.reprator.country.modal.validateCountryIsoCode
import dev.reprator.modals.country.CountryEntity
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

const val ENDPOINT_COUNTRY = "/country"
private const val INPUT_COUNTRY_ID = "countryId"

fun Routing.routeCountry() {

    val countryFacade by inject<CountryController>()

    route(ENDPOINT_COUNTRY) {
        get {
            respondWithResult(HttpStatusCode.OK, countryFacade.getAllCountry())
        }

        get("{$INPUT_COUNTRY_ID}") {
            val countryId = call.parameters[INPUT_COUNTRY_ID].validateCountryIsoCode()
            respondWithResult(HttpStatusCode.OK, countryFacade.getCountry(countryId))
        }

        post {
            val countryInfo =
                call.receiveNullable<CountryEntityDTO>()?.validate() ?: throw IllegalCountryException()
            respondWithResult(HttpStatusCode.Created, countryFacade.addNewCountry(countryInfo))
        }

        put ("{$INPUT_COUNTRY_ID}") {
            val countryId = call.parameters[INPUT_COUNTRY_ID].validateCountryIsoCode()

            val countryInfo = call.receiveNullable<CountryEntityDTO>()?.validate() ?: throw IllegalCountryException()
            respondWithResult(HttpStatusCode.OK, countryFacade.editCountry(countryId, countryInfo))
        }

        patch ("{$INPUT_COUNTRY_ID}") {
            val countryId = call.parameters[INPUT_COUNTRY_ID].validateCountryIsoCode()
            val countryInfo = call.receiveNullable<Map<String, String>>().mapToModal()

            respondWithResult(HttpStatusCode.OK, countryFacade.editCountry(countryId, countryInfo))
        }

        delete("{$INPUT_COUNTRY_ID}") {
            val countryId = call.parameters[INPUT_COUNTRY_ID].validateCountryIsoCode()

            countryFacade.deleteCountry(countryId)
            respondWithResult(HttpStatusCode.OK, true)
        }
    }
}