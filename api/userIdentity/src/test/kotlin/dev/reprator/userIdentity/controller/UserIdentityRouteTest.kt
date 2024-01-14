package dev.reprator.userIdentity.controller

import dev.reprator.base.action.AppDatabaseFactory
import dev.reprator.base.action.TokenStorage
import dev.reprator.base.beans.LENGTH_OTP
import dev.reprator.base.usecase.AppResult
import dev.reprator.commonFeatureImpl.di.JWT_SERVICE
import dev.reprator.commonFeatureImpl.di.koinAppCommonDBModule
import dev.reprator.commonFeatureImpl.di.koinAppCommonModule
import dev.reprator.commonFeatureImpl.di.koinAppNetworkClientModule
import dev.reprator.country.controller.CountryController
import dev.reprator.country.data.TableCountry
import dev.reprator.country.modal.CountryEntityDTO
import dev.reprator.country.setUpKoinCountry
import dev.reprator.modals.user.UserIdentityOTPModal
import dev.reprator.testModule.*
import dev.reprator.testModule.di.SchemaDefinition
import dev.reprator.testModule.di.appTestCoreModule
import dev.reprator.testModule.di.appTestDBModule
import dev.reprator.testModule.di.appTestOverrideModule
import dev.reprator.userIdentity.data.TableUserIdentity
import dev.reprator.userIdentity.data.UserIdentityRepository
import dev.reprator.userIdentity.modal.UserIdentityOtpEntity
import dev.reprator.userIdentity.modal.UserIdentityRegisterEntity
import dev.reprator.userIdentity.modal.UserIdentityRegisterModal
import dev.reprator.userIdentity.module
import dev.reprator.userIdentity.setUpKoinUserIdentityModule
import dev.reprator.userIdentity.socialVerifier.SMScodeGenerator
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
import java.util.concurrent.TimeUnit

@ExtendWith(KtorServerExtension::class)
internal class UserIdentityRouteTest : KoinTest {

    companion object {
        private val INPUT_COUNTRY = CountryEntityDTO("India", 91, "IN")
        private val INPUT_COUNTRY_INVALID_COUNTRY = UserIdentityRegisterEntity.DTO("9041866055", 91)
        private fun INPUT_COUNTRY_VALID_COUNTRY(countryId: Int) =
            UserIdentityRegisterEntity.DTO("9041866055", countryId)
    }

    private val databaseFactory by inject<AppDatabaseFactory>()
    private val controller by inject<UserIdentityController>()
    private val countryController by inject<CountryController>()
    private val tokenStorage by inject<TokenStorage>()

    @JvmField
    @RegisterExtension
    val koinTestExtension = KoinTestExtension.create {

        setUpKoinCountry()
        setUpKoinUserIdentityModule()

        modules(
            koinAppNetworkClientModule,
            koinAppCommonModule(KtorServerExtension.TEST_SERVER!!.environment.config),
            appTestCoreModule,
            appTestDBModule { hikariDataSource, _ ->
                SchemaDefinition.createSchema(hikariDataSource)
            },
            appTestOverrideModule,
            koinAppCommonDBModule,
            module {
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
            }
        )

        KtorServerExtension.TEST_SERVER!!.application.module()
    }

    @BeforeEach
    fun clearDatabase() {
        tokenStorage.cleaToken()
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

    @Test
    fun `Register a user as new user, for valid country id, with otp being sent by server to client successfully`() =
        runTest {

            val countryInserted = countryController.addNewCountry(INPUT_COUNTRY)
            
            val userRegisterResponse = userIdentityClient<UserIdentityRegisterModal>(ACCOUNT_REGISTER) {
                setBody(INPUT_COUNTRY_VALID_COUNTRY(countryInserted.id))
            } as AppResult.Success

            Assertions.assertNotNull(userRegisterResponse)
            Assertions.assertEquals(1, userRegisterResponse.body.userId)

            val userFullModal = controller.getUserById(1)

            Assertions.assertEquals(LENGTH_OTP, userFullModal.phoneOtp.toString().length)
            Assertions.assertEquals(1, userFullModal.otpCount)
        }

    @Test
    fun `Generate otp and send, when user exist in db`() = runTest {

        val countryInserted = countryController.addNewCountry(INPUT_COUNTRY)
        val userRegisterResponse = userIdentityClient<UserIdentityRegisterModal>(ACCOUNT_REGISTER) {
            setBody(INPUT_COUNTRY_VALID_COUNTRY(countryInserted.id))
        } as AppResult.Success

        val generateOtp = userIdentityClient<Boolean>(ACCOUNT_OTP_GENERATE, HttpMethod.Patch) {
            setBody(FormDataContent(Parameters.build {
                append(PARAMETER_USER_ID, "${userRegisterResponse.body.userId}")
            }))
        } as AppResult.Success

        Assertions.assertTrue(generateOtp.body)

        val userFullModal = controller.getUserById(userRegisterResponse.body.userId)

        Assertions.assertEquals(2, userFullModal.otpCount)
    }

    @Test
    fun `Failed to Generate otp as user didn't exist in db`() = runTest {

        val generateOtp = userIdentityClient<Boolean>(ACCOUNT_OTP_GENERATE, HttpMethod.Patch) {
            setBody(FormDataContent(Parameters.build {
                append(PARAMETER_USER_ID, "95")
            }))
        } as AppResult.Error.HttpError

        Assertions.assertEquals(400, generateOtp.code)
    }

    @Test
    fun `Verify user with otp, when user exist in db`() = runTest {

        val countryInserted = countryController.addNewCountry(INPUT_COUNTRY)
        val userRegisterResponse = userIdentityClient<UserIdentityRegisterModal>(ACCOUNT_REGISTER) {
            setBody(INPUT_COUNTRY_VALID_COUNTRY(countryInserted.id))
        } as AppResult.Success

        val userFullModal = controller.getUserById(userRegisterResponse.body.userId)

        val verifyOtp = userIdentityClient<UserIdentityOTPModal>(ACCOUNT_OTP_VERIFY) {
            setBody(UserIdentityOtpEntity.DTO(userFullModal.userId, userFullModal.phoneOtp))
        } as AppResult.Success

        val otpModal = verifyOtp.body

        Assertions.assertTrue(otpModal.isPhoneVerified)
        Assertions.assertTrue(otpModal.refreshToken.trim().isNotEmpty())
        Assertions.assertEquals(userFullModal.userId, otpModal.userId)
        Assertions.assertEquals(userFullModal.phoneNumber, otpModal.phoneNumber)
    }

    @Test
    fun `Failed to verify user with otp as otp didn't match in db for user`() = runTest {

        val countryInserted = countryController.addNewCountry(INPUT_COUNTRY)
        val userRegisterResponse = userIdentityClient<UserIdentityRegisterModal>(ACCOUNT_REGISTER) {
            setBody(INPUT_COUNTRY_VALID_COUNTRY(countryInserted.id))
        } as AppResult.Success

        val userFullModal = controller.getUserById(userRegisterResponse.body.userId)

        val verifyOtp = userIdentityClient<UserIdentityOTPModal>(ACCOUNT_OTP_VERIFY) {
            setBody(UserIdentityOtpEntity.DTO(userFullModal.userId, 3534543))
        } as AppResult.Error.HttpError

        Assertions.assertEquals(400, verifyOtp.code)
    }

    @Test
    fun `Failed to add new user, for invalid country id`(): Unit = runTest {
        val addCountryResponse = userIdentityClient<UserIdentityRegisterModal>(ACCOUNT_REGISTER) {
            setBody(INPUT_COUNTRY_INVALID_COUNTRY)
        }

        Assertions.assertTrue(addCountryResponse is AppResult.Error.GenericError)
        Assertions.assertNotNull(addCountryResponse)
    }

    @Test
    fun `update access and refresh token, if user is logged in successful`() = runTest {

        val countryInserted = countryController.addNewCountry(INPUT_COUNTRY)
        val userRegisterResponse = userIdentityClient<UserIdentityRegisterModal>(ACCOUNT_REGISTER) {
            setBody(INPUT_COUNTRY_VALID_COUNTRY(countryInserted.id))
        } as AppResult.Success

        val userFullModal = controller.getUserById(userRegisterResponse.body.userId)

        val verifyOtp = userIdentityClient<UserIdentityOTPModal>(ACCOUNT_OTP_VERIFY) {
            setBody(UserIdentityOtpEntity.DTO(userFullModal.userId, userFullModal.phoneOtp))
        } as AppResult.Success

        val accessTokenFullModal = userIdentityClient<UserIdentityOTPModal>(ACCOUNT_TOKEN_REFRESH) {
            setBody(FormDataContent(Parameters.build {
                append(PARAMETER_ACCESS_TOKEN, verifyOtp.body.refreshToken)
            }))
        } as AppResult.Success


        Assertions.assertNotEquals(verifyOtp.body.refreshToken, accessTokenFullModal.body.refreshToken)
        Assertions.assertNotEquals(verifyOtp.body.accessToken, accessTokenFullModal.body.accessToken)
    }

    @Test
    fun `logout user, successfully, if token is valid`() = runTest {

        val countryInserted = countryController.addNewCountry(INPUT_COUNTRY)
        val userRegisterResponse = userIdentityClient<UserIdentityRegisterModal>(ACCOUNT_REGISTER) {
            setBody(INPUT_COUNTRY_VALID_COUNTRY(countryInserted.id))
        } as AppResult.Success

        val userFullModal = controller.getUserById(userRegisterResponse.body.userId)

        val verifyOtp = userIdentityClient<UserIdentityOTPModal>(ACCOUNT_OTP_VERIFY) {
            setBody(UserIdentityOtpEntity.DTO(userFullModal.userId, userFullModal.phoneOtp))
        } as AppResult.Success

        tokenStorage.updateToken(verifyOtp.body.accessToken, verifyOtp.body.refreshToken)

        val logoutModal = userIdentityClient<Boolean>(ACCOUNT_LOGOUT) as AppResult.Success

        Assertions.assertTrue(logoutModal.body)
    }

    @Test
    @Timeout(value = 6, unit = TimeUnit.MINUTES)
    fun `logout user, failed, as token is expired after 5 second`() = runBlocking {

        val countryInserted = countryController.addNewCountry(INPUT_COUNTRY)
        val userRegisterResponse = userIdentityClient<UserIdentityRegisterModal>(ACCOUNT_REGISTER) {
            setBody(INPUT_COUNTRY_VALID_COUNTRY(countryInserted.id))
        } as AppResult.Success

        val userFullModal = controller.getUserById(userRegisterResponse.body.userId)

        val verifyOtp = userIdentityClient<UserIdentityOTPModal>(ACCOUNT_OTP_VERIFY) {
            setBody(UserIdentityOtpEntity.DTO(userFullModal.userId, userFullModal.phoneOtp))
        } as AppResult.Success

        tokenStorage.updateToken(verifyOtp.body.accessToken, verifyOtp.body.refreshToken)

        delay(5000)
        val logoutModal = userIdentityClient<Boolean>(ACCOUNT_LOGOUT)

        Assertions.assertTrue(logoutModal is AppResult.Error)
        Assertions.assertEquals(HttpStatusCode.Unauthorized.value, (logoutModal as AppResult.Error.HttpError).code)
    }

    @Test
    @Timeout(value = 6, unit = TimeUnit.SECONDS)
    fun `logout user, if access token is invalid or expired but have valid access token`() = runBlocking {

        val countryInserted = countryController.addNewCountry(INPUT_COUNTRY)
        val userRegisterResponse = userIdentityClient<UserIdentityRegisterModal>(ACCOUNT_REGISTER) {
            setBody(INPUT_COUNTRY_VALID_COUNTRY(countryInserted.id))
        } as AppResult.Success

        val userFullModal = controller.getUserById(userRegisterResponse.body.userId)

        val verifyOtp = userIdentityClient<UserIdentityOTPModal>(ACCOUNT_OTP_VERIFY) {
            setBody(UserIdentityOtpEntity.DTO(userFullModal.userId, userFullModal.phoneOtp))
        } as AppResult.Success

        tokenStorage.updateToken("", verifyOtp.body.refreshToken)

        val logoutModal = userIdentityClient<Boolean>(ACCOUNT_LOGOUT) as AppResult.Success

        Assertions.assertTrue(logoutModal.body)
    }
}


class SMScodeGeneratorTestImpl : SMScodeGenerator {

    override suspend fun sendOtpToMobileNumber(countryCode: Int, phoneNumber: String, messageCode: Int): Boolean = true
}