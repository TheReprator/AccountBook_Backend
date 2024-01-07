package dev.reprator.language.controller

import dev.reprator.core.usecase.FailDTOResponse
import dev.reprator.core.usecase.ResultDTOResponse
import dev.reprator.core.util.dbConfiguration.DatabaseFactory
import dev.reprator.language.data.LanguageRepository
import dev.reprator.language.data.TableLanguage
import dev.reprator.language.domain.LanguageNotFoundException
import dev.reprator.language.modal.LanguageEntity
import dev.reprator.language.modal.LanguageModal
import dev.reprator.language.setUpKoinLanguage
import dev.reprator.testModule.KtorServerExtension
import dev.reprator.testModule.KtorServerExtension.Companion.TEST_BASE_URL
import dev.reprator.testModule.TestDatabaseFactory
import dev.reprator.testModule.createHttpClient
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
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

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(KtorServerExtension::class)
internal class LanguageRouteTest : KoinTest {

    companion object {
        const val LANGUAGE_ENGLISH = "English"
        const val LANGUAGE_HINDI = "Hindi"
    }

    private val databaseFactory by inject<DatabaseFactory>()
    private val languageRepository by inject<LanguageRepository>()

    @JvmField
    @RegisterExtension
    val koinTestExtension = KoinTestExtension.create {

        setUpKoinLanguage()

        modules(
            module {
                singleOf(::TestDatabaseFactory) bind DatabaseFactory::class
            })
    }

    @BeforeEach
    fun clearDatabase() {
        databaseFactory.connect()
        transaction {
            TableLanguage.deleteAll()
        }
    }

    @AfterEach
    fun closeDataBase() {
        databaseFactory.close()
    }

    private fun addLanguageInDb(languageName: String) = runBlocking {
        val client = createHttpClient()
        client.post("$TEST_BASE_URL$ENDPOINT_LANGUAGE") {
            setBody(languageName)
        }
    }

    @Test
    fun `Add new language And Verify from db by id for existence`(): Unit = runBlocking {
        val response = addLanguageInDb(LANGUAGE_ENGLISH)

        Assertions.assertEquals(HttpStatusCode.Created, response.status)
        val resultBody = response.body<ResultDTOResponse<LanguageModal.DTO>>()
        Assertions.assertNotNull(resultBody)

        Assertions.assertEquals(languageRepository.language(resultBody.data.id).name, LANGUAGE_ENGLISH)
        Assertions.assertEquals(resultBody.data.name, LANGUAGE_ENGLISH)
    }

    @Test
    fun `Failed to add new language, if language already exist`(): Unit = runBlocking {
        val addEnglishLanguageResponse = addLanguageInDb(LANGUAGE_ENGLISH)

        Assertions.assertEquals(HttpStatusCode.Created, addEnglishLanguageResponse.status)
        val resultBody = addEnglishLanguageResponse.body<ResultDTOResponse<LanguageModal.DTO>>()
        Assertions.assertNotNull(resultBody)

        Assertions.assertEquals(languageRepository.language(resultBody.data.id).name, LANGUAGE_ENGLISH)
        Assertions.assertEquals(resultBody.data.name, LANGUAGE_ENGLISH)

        val addAgainEnglishLanguageResponse = addLanguageInDb(LANGUAGE_ENGLISH)
        Assertions.assertEquals(addAgainEnglishLanguageResponse.status, HttpStatusCode.InternalServerError)
        val resultBodyAgain = addAgainEnglishLanguageResponse.body<FailDTOResponse>()
        Assertions.assertEquals(resultBodyAgain.statusCode, HttpStatusCode.InternalServerError.value)
        Assertions.assertNotNull(resultBodyAgain)
    }

    @Test
    fun `Get all language from db`(): Unit = runBlocking {
        val languageList = listOf(LANGUAGE_ENGLISH, LANGUAGE_HINDI)
        languageList.forEach {
            addLanguageInDb(it)
        }

        val client = createHttpClient()
        val response = client.get("$TEST_BASE_URL$ENDPOINT_LANGUAGE")

        Assertions.assertEquals(response.status, HttpStatusCode.OK)
        val resultBody = response.body<ResultDTOResponse<List<LanguageModal.DTO>>>()
        Assertions.assertNotNull(resultBody)

        Assertions.assertEquals(resultBody.data.size, languageList.size)
        Assertions.assertEquals(resultBody.data.first().name, languageList.first())
    }

    @Test
    fun `Get language from db by ID, if exist`(): Unit = runBlocking {
        val addLanguageResponse = addLanguageInDb(LANGUAGE_ENGLISH)

        Assertions.assertEquals(HttpStatusCode.Created, addLanguageResponse.status)
        val addResultBody = addLanguageResponse.body<ResultDTOResponse<LanguageModal.DTO>>()
        Assertions.assertNotNull(addResultBody)

        val client = createHttpClient()
        val findResponseSuccess = client.get("$TEST_BASE_URL$ENDPOINT_LANGUAGE/${addResultBody.data.id}")

        Assertions.assertEquals(findResponseSuccess.status, HttpStatusCode.OK)
        val findResultBody = findResponseSuccess.body<ResultDTOResponse<LanguageModal.DTO>>()
        Assertions.assertNotNull(findResultBody)
        Assertions.assertEquals(findResultBody.data.name, LANGUAGE_ENGLISH)
    }

    @Test
    fun `Failed to get language from db by ID, as it didn't exit in db`(): Unit = runBlocking {
        val languageId = 90

        val client = createHttpClient()
        val findResponseSuccess = client.get("$TEST_BASE_URL$ENDPOINT_LANGUAGE/$languageId")

        Assertions.assertEquals(findResponseSuccess.status, HttpStatusCode.NotFound)
        val findResultBody = findResponseSuccess.body<FailDTOResponse>()
        Assertions.assertEquals(HttpStatusCode.NotFound.value, findResultBody.statusCode)
    }

    @Test
    fun `Edit language from db by ID, as it exists`(): Unit = runBlocking {
        val addLanguageResponse = addLanguageInDb(LANGUAGE_ENGLISH)

        val editLanguage = "Khatabook"
        val addResultBody = addLanguageResponse.body<ResultDTOResponse<LanguageModal.DTO>>()
        Assertions.assertNotNull(addResultBody)
        Assertions.assertEquals(LANGUAGE_ENGLISH, addResultBody.data.name)

        val client = createHttpClient()
        val editResponse = client.patch("$TEST_BASE_URL$ENDPOINT_LANGUAGE/${addResultBody.data.id}") {
            contentType(ContentType.Application.Json)
            setBody(LanguageEntity.DTO(addResultBody.data.id, editLanguage))
        }

        val editBody = editResponse.body<ResultDTOResponse<Boolean>>()
        Assertions.assertEquals(HttpStatusCode.OK.value, editBody.statusCode)

        Assertions.assertEquals(editLanguage, languageRepository.language(addResultBody.data.id).name)
    }

    @Test
    fun `Edit language from db by ID got failed, as it didn't exists`(): Unit = runBlocking {
        val languageId = 21

        val client = createHttpClient()
        val editResponse = client.patch("$TEST_BASE_URL$ENDPOINT_LANGUAGE/$languageId") {
            contentType(ContentType.Application.Json)
            setBody(LanguageEntity.DTO(languageId, "vikram"))
        }

        Assertions.assertEquals(editResponse.status, HttpStatusCode.OK)
        val editBody = editResponse.body<ResultDTOResponse<Boolean>>()
        Assertions.assertEquals(editBody.statusCode, HttpStatusCode.OK.value)
        Assertions.assertEquals(editBody.data, false)
    }

    @Test
    fun `Delete language from db by ID, as it exists`(): Unit = runBlocking {
        val addLanguageResponse = addLanguageInDb(LANGUAGE_ENGLISH)

        val addResultBody = addLanguageResponse.body<ResultDTOResponse<LanguageModal.DTO>>()
        Assertions.assertNotNull(addResultBody)
        Assertions.assertEquals(LANGUAGE_ENGLISH, addResultBody.data.name)

        val client = createHttpClient()
        val deleteResponse = client.delete("$TEST_BASE_URL$ENDPOINT_LANGUAGE/${addResultBody.data.id}")

        val editBody = deleteResponse.body<ResultDTOResponse<Boolean>>()
        Assertions.assertEquals(editBody.data, true)
        Assertions.assertEquals(HttpStatusCode.OK.value, editBody.statusCode)

        assertThrows<LanguageNotFoundException> {
            languageRepository.language(addResultBody.data.id)
        }
    }

    @Test
    fun `Delete language from db by ID got failed, as it didn't exists`(): Unit = runBlocking {
        val languageId = 21

        val client = createHttpClient()
        val deleteResponse = client.delete("$TEST_BASE_URL$ENDPOINT_LANGUAGE/$languageId")

        Assertions.assertEquals(deleteResponse.status, HttpStatusCode.OK)
        val editBody = deleteResponse.body<ResultDTOResponse<Boolean>>()
        Assertions.assertEquals(HttpStatusCode.OK.value, editBody.statusCode)
        Assertions.assertEquals(editBody.data, false)
    }
}