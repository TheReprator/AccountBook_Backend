package dev.reprator.country.controller

import dev.reprator.core.usecase.FailDTOResponse
import dev.reprator.core.util.api.ApiResponse
import dev.reprator.core.util.dbConfiguration.DatabaseFactory
import dev.reprator.country.CountryComponent
import dev.reprator.country.data.CountryRepository
import dev.reprator.country.data.TableCountry
import dev.reprator.country.domain.CountryNotFoundException
import dev.reprator.country.modal.CountryEntity
import dev.reprator.country.modal.CountryModal
import dev.reprator.testModule.KtorServerExtension
import dev.reprator.testModule.di.AppCoreComponent
import dev.reprator.testModule.hitApiWithClient
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.ksp.generated.module
import org.koin.test.KoinTest
import org.koin.test.inject
import org.koin.test.junit5.KoinTestExtension

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
        modules( AppCoreComponent().module, CountryComponent().module)
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

    private suspend inline fun <reified T> countryClient(
        endPoint: String = "",
        methodName: HttpMethod = HttpMethod.Post,
        crossinline block: HttpRequestBuilder.() -> Unit = {}
    ) = hitApiWithClient<T>(this.getKoin(), "$ENDPOINT_COUNTRY$endPoint", methodName, block)

    @Test
    fun `Add new country And Verify from db by id for existence`(): Unit = runBlocking {
        val response = countryClient<CountryModal.DTO>{
            setBody(INPUT_COUNTRY)
        } as ApiResponse.Success

        val modal = response.body

        Assertions.assertNotNull(modal)
        Assertions.assertEquals(INPUT_COUNTRY.name, modal.name)
    }

    @Test
    fun `Failed to add new country, for invalid countryCode`(): Unit = runBlocking {
        val addCountryResponse = countryClient<FailDTOResponse>{
            setBody(INPUT_COUNTRY.copy(callingCode = -5))
        } as ApiResponse.Error

        val resultBodyAgain = addCountryResponse as ApiResponse.Error.HttpError
        Assertions.assertEquals(HttpStatusCode.BadRequest.value, resultBodyAgain.code)
        Assertions.assertNotNull(resultBodyAgain)
    }

    @Test
    fun `Failed to add new country, if country already exist`(): Unit = runBlocking {
        val addCountryResponse = countryClient<CountryModal.DTO>{
            setBody(INPUT_COUNTRY)
        } as ApiResponse.Success

        Assertions.assertNotNull(addCountryResponse)
        Assertions.assertEquals(INPUT_COUNTRY.name, addCountryResponse.body.name)

        val resultBodyAgain = countryClient<CountryModal.DTO>{
            setBody(INPUT_COUNTRY)
        } as ApiResponse.Error.HttpError

        Assertions.assertEquals(HttpStatusCode.BadRequest.value, resultBodyAgain.code)
        Assertions.assertNotNull(resultBodyAgain)
    }

    @Test
    fun `Get all country from db`(): Unit = runBlocking {
        val countryInputList = listOf(INPUT_COUNTRY,
            CountryEntity.DTO("Pakistan",92,"PAK"))

        countryInputList.forEach {
            countryClient<CountryModal.DTO>{
                setBody(it)
            }
        }

        val response = countryClient<List<CountryModal.DTO>>(methodName = HttpMethod.Get) as ApiResponse.Success

        Assertions.assertNotNull(response)
        Assertions.assertEquals(response.body.size, countryInputList.size)
        Assertions.assertEquals(response.body.first().name, countryInputList.first().name)
    }

    @Test
    fun `Get country from db by ID, if exist`(): Unit = runBlocking {
        val addResultBody = countryClient<CountryModal.DTO>{
            setBody(INPUT_COUNTRY)
        } as ApiResponse.Success

        Assertions.assertNotNull(addResultBody)

        val findResultBody = countryClient<CountryModal.DTO>(methodName = HttpMethod.Get,
            endPoint = "/${addResultBody.body.id}") as ApiResponse.Success

        Assertions.assertNotNull(findResultBody)
        Assertions.assertEquals(findResultBody.body.shortCode, INPUT_COUNTRY.shortCode)
    }

    @Test
    fun `Failed to get Country from db by ID, as it didn't exit in db`(): Unit = runBlocking {
        val countryId = 90

        val findResultBody = countryClient<CountryModal.DTO>(methodName = HttpMethod.Get,
            endPoint = "/$countryId") as ApiResponse.Error.HttpError

        Assertions.assertEquals(HttpStatusCode.NotFound.value, findResultBody.code)
    }

    @Test
    fun `Update full country, as it exists`(): Unit = runBlocking {
        val addResultBody = countryClient<CountryModal.DTO>{
            setBody(INPUT_COUNTRY)
        } as ApiResponse.Success

        Assertions.assertEquals(INPUT_COUNTRY.callingCode, addResultBody.body.callingCode)

        val changedRequestBody = CountryEntity.DTO("United Arab Emirates",971,"UAE")

        val editResponse = countryClient<Boolean>(methodName = HttpMethod.Put, endPoint = "/${addResultBody.body.id}"){
            contentType(ContentType.Application.Json)
            setBody(changedRequestBody)
        } as ApiResponse.Success

        Assertions.assertTrue(editResponse.body)
        Assertions.assertEquals(changedRequestBody.name, countryRepository.getCountry(addResultBody.body.id).name)
    }

    @Test
    fun `Update of country got failed, as it didn't exists`(): Unit = runBlocking {
        val countryId = 21

        val editResponse = countryClient<FailDTOResponse>(methodName = HttpMethod.Put,
            endPoint = "/$countryId}") {
            setBody(INPUT_COUNTRY)
        } as ApiResponse.Error.HttpError

        Assertions.assertEquals(HttpStatusCode.BadRequest.value, editResponse.code)
    }

    @Test
    fun `Partial update of a country, as it exists`(): Unit = runBlocking {
        val addCountryResponse = countryClient<CountryModal.DTO> {
            setBody(INPUT_COUNTRY)
        } as ApiResponse.Success

        Assertions.assertEquals(INPUT_COUNTRY.callingCode, addCountryResponse.body.callingCode)

        val changedRequestBody = INPUT_COUNTRY.copy(name ="United Arab Emirates")

        val editResponse = countryClient<Boolean>(methodName = HttpMethod.Patch, endPoint = "/${addCountryResponse.body.id}") {
            setBody(changedRequestBody)
        } as ApiResponse.Success

        Assertions.assertTrue(editResponse.body)
        Assertions.assertEquals(changedRequestBody.name, countryRepository.getCountry(addCountryResponse.body.id).name)
    }

    @Test
    fun `Partial update of a country failed, for invalid country name`(): Unit = runBlocking {
        val addResultBody = countryClient<CountryModal.DTO> {
            setBody(INPUT_COUNTRY)
        } as ApiResponse.Success

        Assertions.assertEquals(INPUT_COUNTRY.callingCode, addResultBody.body.callingCode)

        val changedRequestBody = INPUT_COUNTRY.copy(name = "    ")

        val editResponse = countryClient<FailDTOResponse>(methodName = HttpMethod.Patch, endPoint = "/${addResultBody.body.id}") {
            setBody(changedRequestBody)
        } as ApiResponse.Error.HttpError

        Assertions.assertEquals(HttpStatusCode.BadRequest.value, editResponse.code)
    }

    @Test
    fun `Partial update of a country got failed, as it didn't exists`(): Unit = runBlocking {
        val countryId = 21

        val editResponse = countryClient<FailDTOResponse>(methodName = HttpMethod.Patch, endPoint = "/$countryId") {
            setBody(INPUT_COUNTRY)
        } as ApiResponse.Error.HttpError

        Assertions.assertEquals(HttpStatusCode.BadRequest.value, editResponse.code)
    }

    @Test
    fun `Delete country by ID, as it exists`(): Unit = runBlocking {
        val addResultBody = countryClient<CountryModal.DTO> {
            setBody(INPUT_COUNTRY)
        } as ApiResponse.Success

        Assertions.assertEquals(INPUT_COUNTRY.shortCode, addResultBody.body.shortCode)

        val deleteResponse = countryClient<Boolean>(methodName = HttpMethod.Delete, endPoint = "/${addResultBody.body.id}") as ApiResponse.Success

        Assertions.assertTrue(deleteResponse.body)

        assertThrows<CountryNotFoundException> {
            countryRepository.getCountry(addResultBody.body.id)
        }
    }

    @Test
    fun `Delete country by ID got failed, as it didn't exists`(): Unit = runBlocking {
        val countryId = 21

        val deleteResponse = countryClient<FailDTOResponse>(methodName = HttpMethod.Delete, endPoint = "/$countryId") as ApiResponse.Error.HttpError

        Assertions.assertEquals(HttpStatusCode.BadRequest.value, deleteResponse.code)
    }
}