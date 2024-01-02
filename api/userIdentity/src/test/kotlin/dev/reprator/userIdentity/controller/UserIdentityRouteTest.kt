package dev.reprator.userIdentity.controller

import dev.reprator.core.util.api.ApiResponse
import dev.reprator.core.util.api.safeRequest
import dev.reprator.core.util.constants.APIS
import dev.reprator.core.util.dbConfiguration.DatabaseFactory
import dev.reprator.country.data.TableCountry
import dev.reprator.country.data.TableCountryEntity
import dev.reprator.country.setUpKoinCountry
import dev.reprator.testModule.KtorServerExtension
import dev.reprator.testModule.TestDatabaseFactory
import dev.reprator.testModule.setupCoreNetworkModule
import dev.reprator.userIdentity.data.TableUserIdentity
import dev.reprator.userIdentity.modal.UserIdentityRegisterEntity
import dev.reprator.userIdentity.modal.UserIdentityRegisterModal
import dev.reprator.userIdentity.setUpKoinUserIdentityModule
import io.ktor.client.*
import io.ktor.client.request.*
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
        private val INPUT_COUNTRY_INVALID_COUNTRY = UserIdentityRegisterEntity.DTO("9041866055", 91)
    }

    private val databaseFactory by inject<DatabaseFactory>()
    private val client by inject<HttpClient>()

    @JvmField
    @RegisterExtension
    val koinTestExtension = KoinTestExtension.create {

        setUpKoinCountry()
        setUpKoinUserIdentityModule()

        setupCoreNetworkModule()
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
        client.safeRequest<ApiResponse<UserIdentityRegisterModal>>(
            apiType = APIS.INTERNAL_APP,
            attributes = get<Attributes>()
        ) {
            url {
                method = HttpMethod.Post
                path("$ENDPOINT_ACCOUNT/$ACCOUNT_REGISTER")
                contentType(ContentType.Application.Json)
                setBody(countryInfo)
            }
        }
    }

    @Test
    fun `Failed to add new user, for invalid country id`(): Unit = runBlocking {
        val addCountryResponse = addUserInDb(INPUT_COUNTRY_INVALID_COUNTRY)

        Assertions.assertTrue(addCountryResponse is ApiResponse.Error.GenericError)
        Assertions.assertNotNull(addCountryResponse)
    }
}