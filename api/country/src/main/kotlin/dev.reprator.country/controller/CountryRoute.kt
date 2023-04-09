package dev.reprator.country.controller

import dev.reprator.core.ResultResponse
import dev.reprator.country.domain.CountryFacade
import dev.reprator.country.domain.IllegalCountryException
import dev.reprator.country.modal.CountryEntity
import dev.reprator.country.modal.CountryEntity.Companion.mapToModal
import dev.reprator.country.modal.validateCountryIsoCode
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

const val ENDPOINT_COUNTRY = "/country"
private const val INPUT_COUNTRY_ID = "countryId"

fun Routing.routeCountry() {

    val countryFacade by inject<CountryFacade>()

    route(ENDPOINT_COUNTRY) {
        get {
            call.respond(ResultResponse(HttpStatusCode.OK.value, countryFacade.getAllCountry()))
        }

        get("{$INPUT_COUNTRY_ID}") {
            val countryId = call.parameters[INPUT_COUNTRY_ID].validateCountryIsoCode()
            call.respond(ResultResponse(HttpStatusCode.OK.value, countryFacade.getCountry(countryId)))
        }

        post {
            val countryInfo =
                call.receiveNullable<CountryEntity.DTO>()?.validate() ?: throw IllegalCountryException()
            call.respond(ResultResponse(HttpStatusCode.Created.value, countryFacade.addNewCountry(countryInfo)))
        }

        put ("{$INPUT_COUNTRY_ID}") {
            val countryId = call.parameters[INPUT_COUNTRY_ID].validateCountryIsoCode()

            val countryInfo = call.receiveNullable<CountryEntity.DTO>()?.validate() ?: throw IllegalCountryException()
            call.respond(ResultResponse(HttpStatusCode.OK.value, countryFacade.editCountry(countryId, countryInfo)))
        }

        patch ("{$INPUT_COUNTRY_ID}") {
            val countryId = call.parameters[INPUT_COUNTRY_ID].validateCountryIsoCode()
            val countryInfo = call.receiveNullable<Map<String, String>>().mapToModal()

            call.respond(ResultResponse(HttpStatusCode.OK.value, countryFacade.editCountry(countryId, countryInfo)))
        }

        delete("{$INPUT_COUNTRY_ID}") {
            val countryId = call.parameters[INPUT_COUNTRY_ID].validateCountryIsoCode()

            countryFacade.deleteCountry(countryId)
            call.respond(ResultResponse(HttpStatusCode.OK.value, true))
        }
    }
}