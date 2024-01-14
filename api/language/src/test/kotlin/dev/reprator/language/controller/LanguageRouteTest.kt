package dev.reprator.language.controller

import dev.reprator.base.action.AppDatabaseFactory
import dev.reprator.base.usecase.AppResult
import dev.reprator.base.usecase.FailDTOResponse
import dev.reprator.commonFeatureImpl.di.koinAppCommonDBModule
import dev.reprator.commonFeatureImpl.di.koinAppCommonModule
import dev.reprator.commonFeatureImpl.di.koinAppNetworkClientModule
import dev.reprator.language.data.LanguageRepository
import dev.reprator.language.data.TableLanguage
import dev.reprator.language.domain.LanguageNotFoundException
import dev.reprator.language.modal.LanguageEntity
import dev.reprator.language.modal.LanguageModal
import dev.reprator.language.module
import dev.reprator.language.setUpKoinLanguage
import dev.reprator.testModule.KtorServerExtension
import dev.reprator.testModule.di.SchemaDefinition
import dev.reprator.testModule.di.appTestCoreModule
import dev.reprator.testModule.di.appTestDBModule
import dev.reprator.testModule.hitApiWithClient
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.test.KoinTest
import org.koin.test.inject
import org.koin.test.junit5.KoinTestExtension

@ExtendWith(KtorServerExtension::class)
internal class LanguageRouteTest : KoinTest {

    companion object {
        const val LANGUAGE_ENGLISH = "English"
        const val LANGUAGE_HINDI = "Hindi"
    }

    private val databaseFactory by inject<AppDatabaseFactory>()
    private val languageRepository by inject<LanguageRepository>()

    @JvmField
    @RegisterExtension
    val koinTestExtension = KoinTestExtension.create {

        setUpKoinLanguage()

        modules(
            koinAppNetworkClientModule,
            koinAppCommonModule(KtorServerExtension.TEST_SERVER!!.environment.config),
            appTestCoreModule,
            appTestDBModule { hikariDataSource, _ ->
                SchemaDefinition.createSchema(hikariDataSource)
            },
            koinAppCommonDBModule,
        )

        KtorServerExtension.TEST_SERVER!!.application.module()
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

    private suspend inline fun <reified T> languageClient(
        endPoint: String = "",
        methodName: HttpMethod = HttpMethod.Post,
        crossinline block: HttpRequestBuilder.() -> Unit = {}
    ) = hitApiWithClient<T>(this.getKoin(), "$ENDPOINT_LANGUAGE$endPoint", methodName, block)

    @Test
    fun `Add new language And Verify from db by id for existence`(): Unit = runBlocking {

        val resultBody = languageClient<LanguageModal.DTO> {
            setBody(LANGUAGE_ENGLISH)
        } as AppResult.Success

        Assertions.assertNotNull(resultBody)

        Assertions.assertEquals(languageRepository.language(resultBody.body.id).name, LANGUAGE_ENGLISH)
        Assertions.assertEquals(resultBody.body.name, LANGUAGE_ENGLISH)
    }

    @Test
    fun `Failed to add new language, if language already exist`(): Unit = runBlocking {
        val addEnglishLanguageResponse = languageClient<LanguageModal.DTO> {
            setBody(LANGUAGE_ENGLISH)
        } as AppResult.Success

        val resultBody = addEnglishLanguageResponse.body
        Assertions.assertNotNull(addEnglishLanguageResponse.body)
        Assertions.assertEquals(languageRepository.language(resultBody.id).name, LANGUAGE_ENGLISH)
        Assertions.assertEquals(resultBody.name, LANGUAGE_ENGLISH)

        val addAgainEnglishLanguageResponse = languageClient<FailDTOResponse> {
            setBody(LANGUAGE_ENGLISH)
        } as AppResult.Error.GenericError

        Assertions.assertNotNull(addAgainEnglishLanguageResponse.message)
    }

    @Test
    fun `Get all language from db`(): Unit = runBlocking {
        val languageList = listOf(LANGUAGE_ENGLISH, LANGUAGE_HINDI)
        languageList.forEach {
            languageClient<LanguageModal.DTO> {
                setBody(it)
            }
        }

        val resultBody = languageClient<List<LanguageModal.DTO>>(methodName = HttpMethod.Get) as AppResult.Success
        Assertions.assertNotNull(resultBody)

        Assertions.assertEquals(resultBody.body.size, languageList.size)
        Assertions.assertEquals(resultBody.body.first().name, languageList.first())
    }

    @Test
    fun `Get language from db by ID, if exist`(): Unit = runBlocking {
        val addResultBody = languageClient<LanguageModal.DTO> {
            setBody(LANGUAGE_ENGLISH)
        } as AppResult.Success

        Assertions.assertNotNull(addResultBody)

        val findResultBody = languageClient<LanguageModal.DTO>(
            methodName = HttpMethod.Get,
            endPoint = "/${addResultBody.body.id}"
        ) as AppResult.Success

        Assertions.assertNotNull(findResultBody)
        Assertions.assertEquals(findResultBody.body.name, LANGUAGE_ENGLISH)
    }

    @Test
    fun `Failed to get language from db by ID, as it didn't exit in db`(): Unit = runBlocking {
        val languageId = 90

        val findResultBody = languageClient<FailDTOResponse>(
            endPoint = "/$languageId",
            methodName = HttpMethod.Get
        ) as AppResult.Error.HttpError

        Assertions.assertEquals(HttpStatusCode.NotFound.value, findResultBody.code)
        Assertions.assertNotNull(findResultBody.errorBody)
    }

    @Test
    fun `Edit language from db by ID, as it exists`(): Unit = runBlocking {
        val addResultBody = languageClient<LanguageModal.DTO> {
            setBody(LANGUAGE_ENGLISH)
        } as AppResult.Success

        Assertions.assertNotNull(addResultBody)
        Assertions.assertEquals(LANGUAGE_ENGLISH, addResultBody.body.name)

        val editLanguage = "Khatabook"

        val editBody = languageClient<Boolean>(methodName = HttpMethod.Patch, endPoint = "/${addResultBody.body.id}") {
            setBody(LanguageEntity.DTO(addResultBody.body.id, editLanguage))
        } as AppResult.Success

        Assertions.assertTrue(editBody.body)
        Assertions.assertEquals(editLanguage, languageRepository.language(addResultBody.body.id).name)
    }

    @Test
    fun `Edit language from db by ID got failed, as it didn't exists`(): Unit = runBlocking {
        val languageId = 21

        val editBody = languageClient<Boolean>(endPoint = "/$languageId", methodName = HttpMethod.Patch) {
            setBody(LanguageEntity.DTO(languageId, "vikram"))
        } as AppResult.Success

        Assertions.assertFalse(editBody.body)
    }

    @Test
    fun `Delete language from db by ID, as it exists`(): Unit = runBlocking {
        val addResultBody = languageClient<LanguageModal.DTO> {
            setBody(LANGUAGE_ENGLISH)
        } as AppResult.Success

        Assertions.assertNotNull(addResultBody.body)
        Assertions.assertEquals(LANGUAGE_ENGLISH, addResultBody.body.name)

        val editBody = languageClient<Boolean>(methodName = HttpMethod.Delete, endPoint = "/${addResultBody.body.id}") {
            setBody(LANGUAGE_ENGLISH)
        } as AppResult.Success

        Assertions.assertTrue(editBody.body)

        assertThrows<LanguageNotFoundException> {
            languageRepository.language(addResultBody.body.id)
        }
    }

    @Test
    fun `Delete language from db by ID got failed, as it didn't exists`(): Unit = runBlocking {
        val languageId = 21

        val deleteResponse =
            languageClient<Boolean>(endPoint = "/$languageId", methodName = HttpMethod.Delete) as AppResult.Success

        Assertions.assertFalse(deleteResponse.body)
    }
}