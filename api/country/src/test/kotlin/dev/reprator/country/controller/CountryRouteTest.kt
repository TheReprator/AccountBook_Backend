package dev.reprator.country.controller

import dev.reprator.core.DatabaseFactory
import dev.reprator.core.FailResponse
import dev.reprator.core.ResultResponse
import dev.reprator.country.data.CountryRepository
import dev.reprator.country.data.TableCountry
import dev.reprator.country.domain.CountryNotFoundException
import dev.reprator.country.modal.CountryEntity
import dev.reprator.country.modal.CountryModal
import dev.reprator.country.setUpKoinCountry
import dev.reprator.testModule.KtorServerExtension
import dev.reprator.testModule.KtorServerExtension.Companion.BASE_URL
import dev.reprator.testModule.TestDatabaseFactory
import dev.reprator.testModule.createHttpClient
import io.ktor.client.call.*
import io.ktor.client.network.sockets.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.ktor.test.dispatcher.*
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject
import org.koin.test.junit5.KoinTestExtension
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(KtorServerExtension::class)
internal class CountryRouteTest : KoinTest {

    companion object {
        private val INPUT_COUNTRY = CountryEntity.DTO("India",91,"IN")
    }

    private val databaseFactory by inject<DatabaseFactory>()
    private val countryRepository by inject<CountryRepository>()

    @JvmField
    @RegisterExtension
    val koinTestExtension = KoinTestExtension.create {

        setUpKoinCountry()

        modules(
            module {
                singleOf(::TestDatabaseFactory) bind DatabaseFactory::class
            })
    }

    @BeforeEach
    fun clearDatabase() {
        databaseFactory.connect()
        transaction {
            TableCountry.deleteAll()
        }
    }

    @AfterEach
    fun closeDataBase() {
        databaseFactory.close()
    }

    private fun addCountryInDb(countryInfo: CountryEntity.DTO) = runBlocking {
        val client = createHttpClient()
        client.post("$BASE_URL$ENDPOINT_COUNTRY") {
            contentType(ContentType.Application.Json)
            setBody(countryInfo)
        }
    }

    @Test
    fun `Add new country And Verify from db by id for existence`(): Unit = runBlocking {
        val response = addCountryInDb(INPUT_COUNTRY)

        Assertions.assertEquals(response.status, HttpStatusCode.OK)
        val resultBody = response.body<ResultResponse<CountryModal.DTO>>()

        Assertions.assertNotNull(resultBody)
        Assertions.assertEquals(INPUT_COUNTRY.name, resultBody.data.name)
    }

    @Test
    fun `Failed to add new country, for invalid countryCode`(): Unit = runBlocking {
        val addCountryResponse = addCountryInDb(INPUT_COUNTRY.copy(code = -5))

        val resultBodyAgain = addCountryResponse.body<FailResponse>()
        Assertions.assertEquals(HttpStatusCode.BadRequest.value, resultBodyAgain.statusCode)
        Assertions.assertNotNull(resultBodyAgain)
    }

    @Test
    fun `Failed to add new country, if country already exist`(): Unit = runBlocking {
        val addCountryResponse = addCountryInDb(INPUT_COUNTRY)

        Assertions.assertEquals(addCountryResponse.status, HttpStatusCode.OK)
        val resultBody = addCountryResponse.body<ResultResponse<CountryModal.DTO>>()

        Assertions.assertNotNull(resultBody)
        Assertions.assertEquals(INPUT_COUNTRY.name, resultBody.data.name)

        val addAgainSameCountryResponse = addCountryInDb(INPUT_COUNTRY)

        val resultBodyAgain = addAgainSameCountryResponse.body<FailResponse>()
        Assertions.assertEquals(HttpStatusCode.BadRequest.value, resultBodyAgain.statusCode)
        Assertions.assertNotNull(resultBodyAgain)
    }

    @Test
    fun `Get all country from db`(): Unit = runBlocking {
        val countryInputList = listOf(INPUT_COUNTRY,
            CountryEntity.DTO("Pakistan",92,"PAK"))

        countryInputList.forEach {
            addCountryInDb(it)
        }

        val client = createHttpClient()
        val response = client.get("$BASE_URL$ENDPOINT_COUNTRY")

        Assertions.assertEquals(response.status, HttpStatusCode.OK)
        val resultBody = response.body<ResultResponse<List<CountryModal.DTO>>>()
        Assertions.assertNotNull(resultBody)

        Assertions.assertEquals(resultBody.data.size, countryInputList.size)
        Assertions.assertEquals(resultBody.data.first().name, countryInputList.first().name)
    }

    @Test
    fun `Get country from db by ID, if exist`(): Unit = runBlocking {
        val addCountryResponse = addCountryInDb(INPUT_COUNTRY)

        val addResultBody = addCountryResponse.body<ResultResponse<CountryModal.DTO>>()
        Assertions.assertNotNull(addResultBody)

        val client = createHttpClient()
        val findResponseSuccess = client.get("$BASE_URL$ENDPOINT_COUNTRY/${addResultBody.data.id}")

        val findResultBody = findResponseSuccess.body<ResultResponse<CountryModal.DTO>>()
        Assertions.assertNotNull(findResultBody)
        Assertions.assertEquals(findResultBody.data.shortCode, INPUT_COUNTRY.shortCode)
    }

    @Test
    fun `Failed to get Country from db by ID, as it didn't exit in db`(): Unit = runBlocking {
        val countryId = 90

        val client = createHttpClient()
        val findResponseSuccess = client.get("$BASE_URL$ENDPOINT_COUNTRY/$countryId")

        Assertions.assertEquals(findResponseSuccess.status, HttpStatusCode.OK)

        val findResultBody = findResponseSuccess.body<FailResponse>()
        Assertions.assertEquals(HttpStatusCode.NotFound.value, findResultBody.statusCode)
    }


    @Test
    fun `Update full country, as it exists`(): Unit = runBlocking {
        val addCountryResponse = addCountryInDb(INPUT_COUNTRY)

        val addResultBody = addCountryResponse.body<ResultResponse<CountryModal.DTO>>()
        Assertions.assertEquals(INPUT_COUNTRY.code, addResultBody.data.code)

        val changedRequestBody = CountryEntity.DTO("United Arab Emirates",971,"UAE")

        val client = createHttpClient()
        val editResponse = client.put("$BASE_URL$ENDPOINT_COUNTRY/${addResultBody.data.id}") {
            contentType(ContentType.Application.Json)
            setBody(changedRequestBody)
        }

        val editResponseBody = editResponse.body<ResultResponse<Boolean>>()
        Assertions.assertEquals(HttpStatusCode.OK.value, editResponseBody.statusCode)

        Assertions.assertEquals(changedRequestBody.name, countryRepository.getCountry(addResultBody.data.id).name)
    }

    @Test
    fun `Update of country got failed, as it didn't exists`(): Unit = runBlocking {
        val countryId = 21

        val client = createHttpClient()
        val editResponse = client.put("$BASE_URL$ENDPOINT_COUNTRY/$countryId") {
            contentType(ContentType.Application.Json)
            setBody(INPUT_COUNTRY)
        }

        val editBody = editResponse.body<FailResponse>()
        Assertions.assertEquals(HttpStatusCode.BadRequest.value, editBody.statusCode)
    }

    @Test
    fun `Partial update of a country, as it exists`(): Unit = runBlocking {
        val addCountryResponse = addCountryInDb(INPUT_COUNTRY)

        val addResultBody = addCountryResponse.body<ResultResponse<CountryModal.DTO>>()
        Assertions.assertEquals(INPUT_COUNTRY.code, addResultBody.data.code)

        val changedRequestBody = INPUT_COUNTRY.copy(name ="United Arab Emirates")

        val client = createHttpClient()
        val editResponse = client.patch("$BASE_URL$ENDPOINT_COUNTRY/${addResultBody.data.id}") {
            contentType(ContentType.Application.Json)
            setBody(changedRequestBody)
        }

        val editResponseBody = editResponse.body<ResultResponse<Boolean>>()
        Assertions.assertEquals(HttpStatusCode.OK.value, editResponseBody.statusCode)

        Assertions.assertEquals(changedRequestBody.name, countryRepository.getCountry(addResultBody.data.id).name)
    }

    @Test
    fun `Partial update of a country failed, for invalid country name`(): Unit = runBlocking {
        val addCountryResponse = addCountryInDb(INPUT_COUNTRY)

        val addResultBody = addCountryResponse.body<ResultResponse<CountryModal.DTO>>()
        Assertions.assertEquals(INPUT_COUNTRY.code, addResultBody.data.code)

        val changedRequestBody = INPUT_COUNTRY.copy(name = "    ")

        val client = createHttpClient()
        val editResponse = client.patch("$BASE_URL$ENDPOINT_COUNTRY/${addResultBody.data.id}") {
            contentType(ContentType.Application.Json)
            setBody(changedRequestBody)
        }

        val editResponseBody = editResponse.body<FailResponse>()
        Assertions.assertEquals(HttpStatusCode.BadRequest.value, editResponseBody.statusCode)
    }

    @Test
    fun `Partial update of a country got failed, as it didn't exists`(): Unit = runBlocking {
        val countryId = 21

        val client = createHttpClient()
        val editResponse = client.patch("$BASE_URL$ENDPOINT_COUNTRY/$countryId") {
            contentType(ContentType.Application.Json)
            setBody(INPUT_COUNTRY)
        }

        val editBody = editResponse.body<FailResponse>()
        Assertions.assertEquals(HttpStatusCode.BadRequest.value, editBody.statusCode)
    }

    @Test
    fun `Delete country by ID, as it exists`(): Unit = runBlocking {
        val addCountryResponse = addCountryInDb(INPUT_COUNTRY)

        val addResultBody = addCountryResponse.body<ResultResponse<CountryModal.DTO>>()
        Assertions.assertEquals(INPUT_COUNTRY.shortCode, addResultBody.data.shortCode)

        val client = createHttpClient()
        val deleteResponse = client.delete("$BASE_URL$ENDPOINT_COUNTRY/${addResultBody.data.id}")

        val editBody = deleteResponse.body<ResultResponse<Boolean>>()
        Assertions.assertEquals(editBody.data, true)
        Assertions.assertEquals(editBody.statusCode, HttpStatusCode.OK.value)

        assertThrows<CountryNotFoundException> {
            countryRepository.getCountry(addResultBody.data.id)
        }
    }

    @Test
    fun `Delete country by ID got failed, as it didn't exists`(): Unit = runBlocking {
        val countryId = 21

        val client = createHttpClient()
        val deleteResponse = client.delete("$BASE_URL$ENDPOINT_COUNTRY/$countryId")

        val deleteBody = deleteResponse.body<FailResponse>()
        Assertions.assertEquals(HttpStatusCode.BadRequest.value, deleteBody.statusCode)
    }
}