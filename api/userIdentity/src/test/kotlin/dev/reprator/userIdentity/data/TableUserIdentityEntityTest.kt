package dev.reprator.userIdentity.data

import dev.reprator.base.action.AppDatabaseFactory
import dev.reprator.commonFeatureImpl.di.koinAppCommonDBModule
import dev.reprator.country.data.TableCountry
import dev.reprator.country.data.TableCountryEntity
import dev.reprator.modals.country.CountryModal
import dev.reprator.modals.user.USER_CATEGORY
import dev.reprator.modals.user.UserIdentityOTPModal
import dev.reprator.testModule.di.SchemaDefinition
import dev.reprator.testModule.di.appTestDBModule
import dev.reprator.userIdentity.modal.UserIdentityRegisterEntity
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.RegisterExtension
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.koin.test.KoinTest
import org.koin.test.inject
import org.koin.test.junit5.KoinTestExtension
import java.util.stream.Stream

internal class TableUserIdentityEntityTest : KoinTest {

    companion object {

        @JvmStatic
        fun inValidUserInput() = Stream.of(
            Arguments.of(UserIdentityRegisterEntity.DTO("9041866055", 91)),
        )

        @JvmStatic
        fun validUserInput() = Stream.of(
            Arguments.of(UserIdentityRegisterEntity.DTO("9041866055", 1)),
            Arguments.of(UserIdentityRegisterEntity.DTO("507532480", 2))
        )
    }

    private val databaseFactory by inject<AppDatabaseFactory>()

    @JvmField
    @RegisterExtension
    val koinTestExtension = KoinTestExtension.create {
        modules(
            appTestDBModule { hikariDataSource, _ ->
                SchemaDefinition.createSchema(hikariDataSource)
            },
            koinAppCommonDBModule,
        )
    }

    @BeforeEach
    fun insertCountryIntoDatabase() {
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
    fun clearAndCloseDatabase() {
        databaseFactory.close()
    }

    @Test
    fun `Logout user, and set refreshed token to empty with isphone verified as false`() {
        val inputUser = UserIdentityRegisterEntity.DTO("9041866055", 1)

        val insertedUserId = transaction {
            TableUserIdentity.insert {
                it[phoneNumber] = inputUser.phoneNumber
                it[phoneCountryId] = inputUser.countryId
            }.resultedValues?.first()?.get(TableUserIdentity.id) ?: -1
        }

        assertNotEquals(-1, insertedUserId)

        val isLogoutSuccessful = transaction {
            TableUserIdentity.update({ TableUserIdentity.id eq insertedUserId }) {
                it[updateTime] = DateTime.now().toDateTimeISO()
                it[refreshToken] = ""
                it[otpCount] = 0
                it[phoneOtp] = -1
                it[isPhoneVerified] = false
            } > 0
        }

        assertEquals(true, isLogoutSuccessful)

        val userData = transaction {
            (TableUserIdentity innerJoin TableCountryEntity.table)
                .selectAll().where { TableUserIdentity.id eq insertedUserId }.first()
        }

        assertTrue(userData[TableUserIdentity.refreshToken]!!.trim().isEmpty())
        assertTrue(0 ==userData[TableUserIdentity.otpCount])
        assertTrue(-1 ==userData[TableUserIdentity.phoneOtp])
        assertTrue(!userData[TableUserIdentity.isPhoneVerified])
    }

    @Test
    fun `Delete user by id`() {
        val inputUser = UserIdentityRegisterEntity.DTO("9041866055", 1)

        val insertedUserId = transaction {
            TableUserIdentity.insert {
                it[phoneNumber] = inputUser.phoneNumber
                it[phoneCountryId] = inputUser.countryId
            }.resultedValues?.first()?.get(TableUserIdentity.id) ?: -1
        }

        assertNotEquals(-1, insertedUserId)

        val isUserDeleted = transaction {
            TableUserIdentity.deleteWhere {
                id eq insertedUserId
            } > 0
        }

        assertEquals(true, isUserDeleted)
    }

    @Test
    fun `Failed to delete user by id as it didn't exist in db`() {

        val deleteUser = transaction {
            TableUserIdentity.deleteWhere {
                id eq 45
            }
        }

        assertEquals(0, deleteUser)
    }

    @Test
    fun `Get user by id`() {
        val inputUser = UserIdentityRegisterEntity.DTO("9041866055", 1)

        val insertedUserId = transaction {
            TableUserIdentity.insert {
                it[phoneNumber] = inputUser.phoneNumber
                it[phoneCountryId] = inputUser.countryId
            }.resultedValues?.first()?.get(TableUserIdentity.id) ?: -1
        }

        val foundUserCount = transaction {
            (TableUserIdentity innerJoin TableCountryEntity.table)
                .selectAll().where { TableUserIdentity.id eq insertedUserId }.count()
        }

        assertEquals(1, foundUserCount)
    }

    @Test
    fun `Get all inserted User`() {

        val inputList = listOf(UserIdentityRegisterEntity.DTO("9041866055", 1))

        inputList.forEach {
            val inputUser: UserIdentityRegisterEntity.DTO = it

            transaction {
                TableUserIdentity.insert { inner ->
                    inner[phoneNumber] = inputUser.phoneNumber
                    inner[phoneCountryId] = inputUser.countryId
                }
            }
        }

        val userList = transaction {
            (TableUserIdentity innerJoin TableCountryEntity.table)
                .selectAll().map { from ->

                    val countryEntity = TableCountryEntity.wrapRow(from)
                    val countryModal = CountryModal.DTO(
                        countryEntity.id.value,
                        countryEntity.name, countryEntity.isocode, countryEntity.shortcode
                    )

                    UserIdentityOTPModal.DTO(
                        from[TableUserIdentity.id], from[TableUserIdentity.phoneNumber].toString(),
                        from[TableUserIdentity.isPhoneVerified], countryModal,
                        from[TableUserIdentity.refreshToken] ?: "", "",
                        from[TableUserIdentity.userType]
                    )
                }
        }

        assertEquals(inputList.size, userList.size)
        assertNotEquals(null, userList.first().refreshToken)
        assertNotEquals(USER_CATEGORY.employee, userList.first().userType)
        assertEquals(USER_CATEGORY.owner, userList.first().userType)
        assertEquals(inputList.first().phoneNumber, userList.first().phoneNumber)
    }

    @ParameterizedTest
    @MethodSource("validUserInput")
    fun `Register a user with valid country id`(
        inputUser: UserIdentityRegisterEntity.DTO
    ) {
        val insertedUserId = transaction {
            TableUserIdentity.insert {
                it[phoneNumber] = inputUser.phoneNumber
                it[phoneCountryId] = inputUser.countryId
            }.resultedValues?.first()?.get(TableUserIdentity.id) ?: -1
        }

        assertNotNull(insertedUserId)
        assertNotEquals(-1, insertedUserId)
    }

    @ParameterizedTest
    @MethodSource("inValidUserInput")
    fun `Insertion failed to invalid input, as country id didn't exist`(
        inputUser: UserIdentityRegisterEntity.DTO
    ) {
        assertThrows<ExposedSQLException> {
            transaction {
                TableUserIdentity.insert {
                    it[phoneNumber] = inputUser.phoneNumber
                    it[phoneCountryId] = inputUser.countryId
                }
            }
        }
    }
}