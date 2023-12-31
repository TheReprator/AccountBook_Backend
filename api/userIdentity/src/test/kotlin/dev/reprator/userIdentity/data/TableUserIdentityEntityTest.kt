package dev.reprator.userIdentity.data

import dev.reprator.core.DatabaseFactory
import dev.reprator.country.data.TableCountry
import dev.reprator.country.data.TableCountryEntity
import dev.reprator.country.modal.CountryModal
import dev.reprator.testModule.KtorServerExtension
import dev.reprator.testModule.TestDatabaseFactory
import dev.reprator.userIdentity.modal.UserIdentityOTPModal
import dev.reprator.userIdentity.modal.UserIdentityRegisterEntity
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.RegisterExtension
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject
import org.koin.test.junit5.KoinTestExtension
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(KtorServerExtension::class)
internal class TableUserIdentityEntityTest : KoinTest {

    companion object {

        @JvmStatic
        fun inValidUserInput() = Stream.of(
            Arguments.of(UserIdentityRegisterEntity.DTO("9041866055",91)),
        )

        @JvmStatic
        fun validUserInput() = Stream.of(
            Arguments.of(UserIdentityRegisterEntity.DTO("9041866055",1)),
            Arguments.of(UserIdentityRegisterEntity.DTO("507532480",2))
        )
    }

    private val databaseFactory by inject<DatabaseFactory>()

    @JvmField
    @RegisterExtension
    val koinTestExtension = KoinTestExtension.create {
        modules(
            module {
                singleOf(::TestDatabaseFactory) bind DatabaseFactory::class
            })
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
    fun `Delete user by id`() {
        val inputUser = UserIdentityRegisterEntity.DTO("9041866055",1)

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
        val inputUser = UserIdentityRegisterEntity.DTO("9041866055",1)

        val insertedUserId = transaction {
            TableUserIdentity.insert {
                it[phoneNumber] = inputUser.phoneNumber
                it[phoneCountryId] = inputUser.countryId
            }.resultedValues?.first()?.get(TableUserIdentity.id) ?: -1
        }

        val foundUserCount = transaction {
            (TableUserIdentity innerJoin TableCountryEntity.table)
                .select { TableUserIdentity.id eq insertedUserId }.count()
        }

        assertEquals(1, foundUserCount)
    }

    @Test
    fun `Failed to get user by id as it didn't exist in db`() {

        val foundCountry = transaction {
            TableUserIdentity.select { TableUserIdentity.id eq 48 }.count()
        }

        assertEquals(0, foundCountry)
    }

    @Test
    fun `Get all inserted User`() {

        val inputList = listOf(UserIdentityRegisterEntity.DTO("9041866055",1),
        UserIdentityRegisterEntity.DTO("507532480",2))

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
                     ""
                )
            }
        }

        assertEquals(inputList.size, userList.size)
        assertEquals(inputList.first().phoneNumber, userList.first().phoneNumber)
    }

    @ParameterizedTest
    @MethodSource("validUserInput")
    fun `Tests for the successful insertion of valid country`(
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