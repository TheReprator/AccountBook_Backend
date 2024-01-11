package dev.reprator.userIdentity.controller

import dev.reprator.core.util.api.ApiResponse
import dev.reprator.core.util.constants.LENGTH_OTP
import dev.reprator.core.util.dbConfiguration.DatabaseFactory
import dev.reprator.country.controller.CountryController
import dev.reprator.country.data.TableCountry
import dev.reprator.country.modal.CountryEntity
import dev.reprator.country.setUpKoinCountry
import dev.reprator.testModule.*
import dev.reprator.userIdentity.data.TableUserIdentity
import dev.reprator.userIdentity.data.UserIdentityRepository
import dev.reprator.userIdentity.modal.UserIdentityOTPModal
import dev.reprator.userIdentity.modal.UserIdentityOtpEntity
import dev.reprator.userIdentity.modal.UserIdentityRegisterEntity
import dev.reprator.userIdentity.modal.UserIdentityRegisterModal
import dev.reprator.userIdentity.setUpKoinUserIdentityModule
import dev.reprator.userIdentity.socialVerifier.SMScodeGenerator
import impl.DatabaseFactoryImpl
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
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
                singleOf(::DatabaseFactoryImpl) bind DatabaseFactory::class
                singleOf(::SMScodeGeneratorTestImpl) bind SMScodeGenerator::class
                single<(Int) -> Boolean>(named(JWT_SERVICE)) {
                    val isUserValid:(Int) -> Boolean = {
                        runBlocking {
                            val userController = get<UserIdentityRepository>()
                            try {
                                val result = userController.getUserById(it)
                                result.refreshToken.trim().isNotEmpty()
                            } catch (exception: Exception) {
                                false
                            }
                        }
                    }
                    isUserValid
                }
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

    private suspend inline fun <reified T> userIdentityClient(
        endPoint: String = "",
        methodName: HttpMethod = HttpMethod.Post,
        crossinline block: HttpRequestBuilder.() -> Unit = {}
    ) = hitApiWithClient<T>(this.getKoin(), "$ENDPOINT_ACCOUNT/$endPoint", methodName, block)

    @RepeatedTest(2)
    fun `Register a user as new user, for valid country id, with otp being sent by server to client successfully`() =
        runTest {

            val countryInserted = countryController.addNewCountry(INPUT_COUNTRY)
            
            val userRegisterResponse = userIdentityClient<UserIdentityRegisterModal>(ACCOUNT_REGISTER) {
                setBody(INPUT_COUNTRY_VALID_COUNTRY(countryInserted.id))
            } as ApiResponse.Success

            Assertions.assertNotNull(userRegisterResponse)
            Assertions.assertEquals(1, userRegisterResponse.body.userId)

            val userFullModal = controller.getUserById(1)
            Assertions.assertEquals(LENGTH_OTP, userFullModal.phoneOtp.toString().length)
            Assertions.assertEquals(1, userFullModal.otpCount)
        }

    @Test
    fun `Generate otp and send, when user exist in db`() = runBlocking {

        val countryInserted = countryController.addNewCountry(INPUT_COUNTRY)
        val userRegisterResponse = userIdentityClient<UserIdentityRegisterModal>(ACCOUNT_REGISTER) {
            setBody(INPUT_COUNTRY_VALID_COUNTRY(countryInserted.id))
        } as ApiResponse.Success

        val generateOtp = userIdentityClient<Boolean>(ACCOUNT_OTP_GENERATE, HttpMethod.Patch) {
            setBody(FormDataContent(Parameters.build {
                append(PARAMETER_USER_ID, "${userRegisterResponse.body.userId}")
            }))
        } as ApiResponse.Success

        Assertions.assertTrue(generateOtp.body)

        val userFullModal = controller.getUserById(userRegisterResponse.body.userId)
        Assertions.assertEquals(2, userFullModal.otpCount)
    }

    @Test
    fun `Failed to Generate otp as user didn't exist in db`() = runBlocking {

        val generateOtp = userIdentityClient<Boolean>(ACCOUNT_OTP_GENERATE, HttpMethod.Patch) {
            setBody(FormDataContent(Parameters.build {
                append(PARAMETER_USER_ID, "95")
            }))
        } as ApiResponse.Error.HttpError

        Assertions.assertEquals(400, generateOtp.code)
    }

    @Test
    fun `Verify user with otp, when user exist in db`() = runTest {

        val countryInserted = countryController.addNewCountry(INPUT_COUNTRY)
        val userRegisterResponse = userIdentityClient<UserIdentityRegisterModal>(ACCOUNT_REGISTER) {
            setBody(INPUT_COUNTRY_VALID_COUNTRY(countryInserted.id))
        } as ApiResponse.Success

        val userFullModal = controller.getUserById(userRegisterResponse.body.userId)

        val verifyOtp = userIdentityClient<UserIdentityOTPModal>(ACCOUNT_OTP_VERIFY) {
            setBody(UserIdentityOtpEntity.DTO(userFullModal.userId, userFullModal.phoneOtp))
        } as ApiResponse.Success

        val otpModal = verifyOtp.body

        Assertions.assertTrue(otpModal.isPhoneVerified)
        Assertions.assertTrue(otpModal.refreshToken.trim().isNotEmpty())
        Assertions.assertEquals(userFullModal.userId, otpModal.userId)
        Assertions.assertEquals(userFullModal.phoneNumber, otpModal.phoneNumber)
    }

    @Test
    fun `Failed to verify user with otp as otp didn't match in db for user`() = runBlocking {

        val countryInserted = countryController.addNewCountry(INPUT_COUNTRY)
        val userRegisterResponse = userIdentityClient<UserIdentityRegisterModal>(ACCOUNT_REGISTER) {
            setBody(INPUT_COUNTRY_VALID_COUNTRY(countryInserted.id))
        } as ApiResponse.Success

        val userFullModal = controller.getUserById(userRegisterResponse.body.userId)

        val verifyOtp = userIdentityClient<UserIdentityOTPModal>(ACCOUNT_OTP_VERIFY) {
            setBody(UserIdentityOtpEntity.DTO(userFullModal.userId, 3534543))
        } as ApiResponse.Error.HttpError

        Assertions.assertEquals(400, verifyOtp.code)
    }

    @Test
    fun `Failed to add new user, for invalid country id`(): Unit = runBlocking {
        val addCountryResponse = userIdentityClient<UserIdentityRegisterModal>(ACCOUNT_REGISTER) {
            setBody(INPUT_COUNTRY_INVALID_COUNTRY)
        }

        Assertions.assertTrue(addCountryResponse is ApiResponse.Error.GenericError)
        Assertions.assertNotNull(addCountryResponse)
    }

    @Test
    fun `update access and refresh token, if user is logged in successful`() = runBlocking {

        val countryInserted = countryController.addNewCountry(INPUT_COUNTRY)
        val userRegisterResponse = userIdentityClient<UserIdentityRegisterModal>(ACCOUNT_REGISTER) {
            setBody(INPUT_COUNTRY_VALID_COUNTRY(countryInserted.id))
        } as ApiResponse.Success

        val userFullModal = controller.getUserById(userRegisterResponse.body.userId)

        val verifyOtp = userIdentityClient<UserIdentityOTPModal>(ACCOUNT_OTP_VERIFY) {
            setBody(UserIdentityOtpEntity.DTO(userFullModal.userId, userFullModal.phoneOtp))
        } as ApiResponse.Success

        val accessTokenFullModal = userIdentityClient<UserIdentityOTPModal>(ACCOUNT_TOKEN_REFRESH) {
            setBody(FormDataContent(Parameters.build {
                append(PARAMETER_ACCESS_TOKEN, verifyOtp.body.refreshToken)
            }))
        } as ApiResponse.Success


        Assertions.assertNotEquals(verifyOtp.body.refreshToken, accessTokenFullModal.body.refreshToken)
        Assertions.assertNotEquals(verifyOtp.body.accessToken, accessTokenFullModal.body.accessToken)
    }

    @Test
    fun `logout user, successfully, if token is valid`() = runBlocking {

        val countryInserted = countryController.addNewCountry(INPUT_COUNTRY)
        val userRegisterResponse = userIdentityClient<UserIdentityRegisterModal>(ACCOUNT_REGISTER) {
            setBody(INPUT_COUNTRY_VALID_COUNTRY(countryInserted.id))
        } as ApiResponse.Success

        val userFullModal = controller.getUserById(userRegisterResponse.body.userId)

        val verifyOtp = userIdentityClient<UserIdentityOTPModal>(ACCOUNT_OTP_VERIFY) {
            setBody(UserIdentityOtpEntity.DTO(userFullModal.userId, userFullModal.phoneOtp))
        } as ApiResponse.Success

        TOKEN_ACCESS = verifyOtp.body.accessToken
        TOKEN_REFRESH = verifyOtp.body.refreshToken

        val logoutModal = userIdentityClient<Boolean>(ACCOUNT_LOGOUT) as ApiResponse.Success

        Assertions.assertTrue(logoutModal.body)
    }

    //@RepeatedTest(2)
    @Test
    fun `logout user, failed, as token is expired after 7 second for testing`() = runBlocking {

        val countryInserted = countryController.addNewCountry(INPUT_COUNTRY)
        val userRegisterResponse = userIdentityClient<UserIdentityRegisterModal>(ACCOUNT_REGISTER) {
            setBody(INPUT_COUNTRY_VALID_COUNTRY(countryInserted.id))
        } as ApiResponse.Success

        val userFullModal = controller.getUserById(userRegisterResponse.body.userId)

        val verifyOtp = userIdentityClient<UserIdentityOTPModal>(ACCOUNT_OTP_VERIFY) {
            setBody(UserIdentityOtpEntity.DTO(userFullModal.userId, userFullModal.phoneOtp))
        } as ApiResponse.Success

        TOKEN_ACCESS = verifyOtp.body.accessToken
        TOKEN_REFRESH = verifyOtp.body.refreshToken

        delay(3_000)
        val logoutModal = userIdentityClient<Boolean>(ACCOUNT_LOGOUT)

        Assertions.assertTrue(logoutModal is ApiResponse.Error)
        Assertions.assertEquals(401, (logoutModal as ApiResponse.Error.HttpError).code)
    }
}


class SMScodeGeneratorTestImpl : SMScodeGenerator {

    override suspend fun sendOtpToMobileNumber(countryCode: Int, phoneNumber: String, messageCode: Int): Boolean = true
}