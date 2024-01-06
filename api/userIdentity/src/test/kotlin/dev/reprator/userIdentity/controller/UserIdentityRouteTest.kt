package dev.reprator.userIdentity.controller

import dev.reprator.core.usecase.ResultDTOResponse
import dev.reprator.core.util.api.ApiResponse
import dev.reprator.core.util.api.safeRequest
import dev.reprator.core.util.constants.APIS
import dev.reprator.core.util.constants.LENGTH_OTP
import dev.reprator.core.util.dbConfiguration.DatabaseFactory
import dev.reprator.country.controller.CountryController
import dev.reprator.country.data.TableCountry
import dev.reprator.country.modal.CountryEntity
import dev.reprator.country.setUpKoinCountry
import dev.reprator.testModule.KtorServerExtension
import dev.reprator.testModule.TestDatabaseFactory
import dev.reprator.testModule.setupCoreNetworkModule
import dev.reprator.userIdentity.data.TableUserIdentity
import dev.reprator.userIdentity.modal.UserIdentityOTPModal
import dev.reprator.userIdentity.modal.UserIdentityOtpEntity
import dev.reprator.userIdentity.modal.UserIdentityRegisterEntity
import dev.reprator.userIdentity.modal.UserIdentityRegisterModal
import dev.reprator.userIdentity.setUpKoinUserIdentityModule
import dev.reprator.userIdentity.socialVerifier.SMScodeGenerator
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.component.get
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
        private val INPUT_COUNTRY = CountryEntity.DTO("India", 91, "IN")
        private val INPUT_COUNTRY_INVALID_COUNTRY = UserIdentityRegisterEntity.DTO("9041866055", 91)
        private fun INPUT_COUNTRY_VALID_COUNTRY(countryId: Int) =
            UserIdentityRegisterEntity.DTO("9041866055", countryId)
    }

    private val databaseFactory by inject<DatabaseFactory>()
    private val controller by inject<UserIdentityController>()
    private val countryController by inject<CountryController>()

    @JvmField
    @RegisterExtension
    val koinTestExtension = KoinTestExtension.create {

        setUpKoinCountry()
        setUpKoinUserIdentityModule()

        setupCoreNetworkModule()
        modules(
            module {
                singleOf(::TestDatabaseFactory) bind DatabaseFactory::class
                singleOf(::SMScodeGeneratorTestImpl) bind SMScodeGenerator::class
            })
    }

    @BeforeEach
    fun clearDatabase() {
        databaseFactory.connect()

        transaction {
            TableCountry.deleteAll()
            TableUserIdentity.deleteAll()
        }
    }

    @AfterEach
    fun closeDataBase() {
        databaseFactory.close()
    }

    private suspend inline fun <reified T> clientBody(
        endPoint: String,
        methodName: HttpMethod = HttpMethod.Post,
        crossinline block: HttpRequestBuilder.() -> Unit
    ) =
        get<HttpClient>().safeRequest<T>(
            apiType = APIS.INTERNAL_APP,
            attributes = get<Attributes>()
        ) {
            url {
                method = methodName
                path("$ENDPOINT_ACCOUNT/$endPoint")
            }
            contentType(ContentType.Application.Json)
            block(this@safeRequest)
        }

    @Test
    fun `Register a user as new user, for valid country id, with otp being sent by server to client successfully`() =
        runBlocking {

            val countryInserted = countryController.addNewCountry(INPUT_COUNTRY)
            val userRegisterResponse = clientBody<ResultDTOResponse<UserIdentityRegisterModal>>(ACCOUNT_REGISTER) {
                setBody(INPUT_COUNTRY_VALID_COUNTRY(countryInserted.id))
            }

            Assertions.assertTrue(userRegisterResponse is ApiResponse.Success)
            Assertions.assertNotNull(userRegisterResponse)
            val body = (userRegisterResponse as ApiResponse.Success).body
            Assertions.assertEquals(1, body.data.userId)

            val userFullModal = controller.getUserById(1)
            Assertions.assertEquals(LENGTH_OTP, userFullModal.phoneOtp.toString().length)
            Assertions.assertEquals(1, userFullModal.otpCount)
        }

    @Test
    fun `Generate otp and send, when user exist in db`() = runBlocking {

        val countryInserted = countryController.addNewCountry(INPUT_COUNTRY)
        val userRegisterResponse = clientBody<ResultDTOResponse<UserIdentityRegisterModal>>(ACCOUNT_REGISTER) {
            setBody(INPUT_COUNTRY_VALID_COUNTRY(countryInserted.id))
        } as ApiResponse.Success

        val generateOtp = clientBody<ResultDTOResponse<Boolean>>(ACCOUNT_OTP_GENERATE, HttpMethod.Patch) {
            setBody(FormDataContent(Parameters.build {
                append(PARAMETER_USER_ID, "${userRegisterResponse.body.data.userId}")
            }))
        } as ApiResponse.Success

        Assertions.assertTrue(generateOtp.body.data)

        val userFullModal = controller.getUserById(userRegisterResponse.body.data.userId)
        Assertions.assertEquals(2, userFullModal.otpCount)
    }

    @Test
    fun `Failed to Generate otp as user didn't exist in db`() = runBlocking {

        val generateOtp = clientBody<ResultDTOResponse<Boolean>>(ACCOUNT_OTP_GENERATE, HttpMethod.Patch) {
            setBody(FormDataContent(Parameters.build {
                append(PARAMETER_USER_ID, "95")
            }))
        } as ApiResponse.Error.HttpError

        Assertions.assertEquals(400, generateOtp.code)
    }

    @Test
    fun `Verify user with otp, when user exist in db`() = runBlocking {

        val countryInserted = countryController.addNewCountry(INPUT_COUNTRY)
        val userRegisterResponse = clientBody<ResultDTOResponse<UserIdentityRegisterModal>>(ACCOUNT_REGISTER) {
            setBody(INPUT_COUNTRY_VALID_COUNTRY(countryInserted.id))
        } as ApiResponse.Success

        val userFullModal = controller.getUserById(userRegisterResponse.body.data.userId)

        val verifyOtp = clientBody<ResultDTOResponse<UserIdentityOTPModal>>(ACCOUNT_OTP_VERIFY) {
            setBody(UserIdentityOtpEntity.DTO(userFullModal.userId, userFullModal.phoneOtp))
        } as ApiResponse.Success

        val otpModal = verifyOtp.body.data

        Assertions.assertTrue(otpModal.isPhoneVerified)
        //Assertions.assertTrue(1== otpModal.refreshToken.length)
        Assertions.assertEquals(userFullModal.userId, otpModal.userId)
        Assertions.assertEquals(userFullModal.phoneNumber, otpModal.phoneNumber)
    }

    @Test
    fun `Failed to verify user with otp as otp didn't match in db for user`() = runBlocking {

        val countryInserted = countryController.addNewCountry(INPUT_COUNTRY)
        val userRegisterResponse = clientBody<ResultDTOResponse<UserIdentityRegisterModal>>(ACCOUNT_REGISTER) {
            setBody(INPUT_COUNTRY_VALID_COUNTRY(countryInserted.id))
        } as ApiResponse.Success

        val userFullModal = controller.getUserById(userRegisterResponse.body.data.userId)

        val verifyOtp = clientBody<ResultDTOResponse<UserIdentityOTPModal>>(ACCOUNT_OTP_VERIFY) {
            setBody(UserIdentityOtpEntity.DTO(userFullModal.userId, 3534543))
        } as ApiResponse.Error.HttpError

        Assertions.assertEquals(400, verifyOtp.code)
    }

    @Test
    fun `Failed to add new user, for invalid country id`(): Unit = runBlocking {
        val addCountryResponse = clientBody<ResultDTOResponse<UserIdentityRegisterModal>>(ACCOUNT_REGISTER) {
            setBody(INPUT_COUNTRY_INVALID_COUNTRY)
        }

        Assertions.assertTrue(addCountryResponse is ApiResponse.Error.GenericError)
        Assertions.assertNotNull(addCountryResponse)
    }
}


class SMScodeGeneratorTestImpl : SMScodeGenerator {

    override suspend fun sendOtpToMobileNumber(countryCode: Int, phoneNumber: String, messageCode: Int): Boolean = true
}