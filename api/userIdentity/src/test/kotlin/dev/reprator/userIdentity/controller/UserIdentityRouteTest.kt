package dev.reprator.userIdentity.controller

import dev.reprator.core.DatabaseFactory
import dev.reprator.core.FailResponse
import dev.reprator.core.ResultResponse
import dev.reprator.country.data.TableCountry
import dev.reprator.country.data.TableCountryEntity
import dev.reprator.country.setUpKoinCountry
import dev.reprator.testModule.KtorServerExtension
import dev.reprator.testModule.KtorServerExtension.Companion.BASE_URL
import dev.reprator.testModule.TestDatabaseFactory
import dev.reprator.testModule.createHttpClient
import dev.reprator.userIdentity.data.TableUserIdentity
import dev.reprator.userIdentity.data.UserIdentityRepository
import dev.reprator.userIdentity.modal.UserIdentityRegisterEntity
import dev.reprator.userIdentity.modal.UserIdentityRegisterModal
import dev.reprator.userIdentity.setUpKoinUserIdentityModule
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
internal class UserIdentityRouteTest : KoinTest {

    companion object {
        private val INPUT_COUNTRY = UserIdentityRegisterEntity.DTO("9041866055",1)
        private val INPUT_COUNTRY_UAE = UserIdentityRegisterEntity.DTO("507532480",2)
        private val INPUT_COUNTRY_INVALID_COUNTRY = UserIdentityRegisterEntity.DTO("9041866055",91)
    }

    private val databaseFactory by inject<DatabaseFactory>()
    private val userIdentityRepository by inject<UserIdentityRepository>()

    @JvmField
    @RegisterExtension
    val koinTestExtension = KoinTestExtension.create {

        setUpKoinCountry()
        setUpKoinUserIdentityModule()

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
            TableUserIdentity.deleteAll()
            
            TableCountryEntity.new {
                name = "India"
                shortcode = "IN"
                isocode = 91
            }

            TableCountryEntity.new {
                name = "United Arab Emirates"
                shortcode = "UAE"
                isocode = 971
            }
        }
    }

    @AfterEach
    fun closeDataBase() {
        databaseFactory.close()
    }

    private fun addUserInDb(countryInfo: UserIdentityRegisterEntity.DTO) = runBlocking {
        val client = createHttpClient()
        client.post("$BASE_URL$ENDPOINT_ACCOUNT/$ACCOUNT_REGISTER") {
            contentType(ContentType.Application.Json)
            setBody(countryInfo)
        }
    }

    @Test
    fun `Add new user and get registered UserId`(): Unit = runBlocking {
//        val response = addUserInDb(INPUT_COUNTRY)
//
//        Assertions.assertEquals(response.status, HttpStatusCode.OK)
//        val resultBody = response.body<ResultResponse<UserIdentityRegisterModal.DTO>>()
//
//        Assertions.assertNotNull(resultBody)
//        Assertions.assertEquals(1, resultBody.data.userId)
    }

    @Test
    fun `Failed to add new user, for invalid country id`(): Unit = runBlocking {
        val addCountryResponse = addUserInDb(INPUT_COUNTRY_INVALID_COUNTRY)

        val resultBodyAgain = addCountryResponse.body<FailResponse>()
        Assertions.assertEquals(HttpStatusCode.InternalServerError.value, resultBodyAgain.statusCode)
        Assertions.assertNotNull(resultBodyAgain)
    }

//    @Test
//    fun `Get otp, if user exit or it's mobile no exist in db with valid country id`(): Unit = runBlocking {
//        val addCountryResponse = addUserInDb(INPUT_COUNTRY)
//
//        Assertions.assertEquals(addCountryResponse.status, HttpStatusCode.OK)
//        val resultBody = addCountryResponse.body<ResultResponse<UserIdentityRegisterModal.DTO>>()
//
//        var otp: Int = -1
//
//        transaction {
//            TableUserIdentity.select {
//                TableUserIdentity.id eq resultBody.data.userId
//            }.forEach {
//                otp = it[TableUserIdentity.phoneOtp] ?: -1
//            }
//        }
//
//        Assertions.assertEquals(INPUT_COUNTRY.name, resultBody.data.name)
//
//        val addAgainSameCountryResponse = addUserInDb(INPUT_COUNTRY)
//
//        val resultBodyAgain = addAgainSameCountryResponse.body<FailResponse>()
//        Assertions.assertEquals(HttpStatusCode.BadRequest.value, resultBodyAgain.statusCode)
//        Assertions.assertNotNull(resultBodyAgain)
//    }
}