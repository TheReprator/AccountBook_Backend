package dev.reprator.country.data

import dev.reprator.base.action.AppDatabaseFactory
import dev.reprator.commonFeatureImpl.di.koinAppCommonDBModule
import dev.reprator.country.modal.CountryEntityDTO
import dev.reprator.modals.country.CountryModal
import dev.reprator.testModule.KtorServerExtension
import dev.reprator.testModule.di.SchemaDefinition
import dev.reprator.testModule.di.appTestDBModule
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.RegisterExtension
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.koin.test.KoinTest
import org.koin.test.inject
import org.koin.test.junit5.KoinTestExtension
import java.util.stream.Stream

@ExtendWith(KtorServerExtension::class)
internal class TableCountryEntityTest : KoinTest {

    companion object {

        @JvmStatic
        fun inValidUpdateCountryInput() = Stream.of(
            Arguments.of(CountryEntityDTO("123", 91, "IN")),
        )

        @JvmStatic
        fun validUpdateCountryInput() = Stream.of(
            Arguments.of(CountryEntityDTO("India", 91, "UAE")),
            Arguments.of(CountryEntityDTO("India", 971, "IN")),
            Arguments.of(CountryEntityDTO("United Arab Emirate", 91, "IN")),
            Arguments.of(CountryEntityDTO("United Arab Emirate", 91, "IN")),
            Arguments.of(CountryEntityDTO("India", 91, "IN")),
            Arguments.of(CountryEntityDTO("", 91, "IN")),
            Arguments.of(CountryEntityDTO("  ", 91, "IN")),
            Arguments.of(CountryEntityDTO("dddf", 91, "IN")),
            Arguments.of(CountryEntityDTO("India", -1, "IN")),
            Arguments.of(CountryEntityDTO("India", 91, "")),
        )

        @JvmStatic
        fun validCountryInput() = Stream.of(
            Arguments.of(CountryEntityDTO("India", 91, "IN")),
            Arguments.of(CountryEntityDTO("   India", 91, "IN")),
            Arguments.of(CountryEntityDTO("234234", 91, "IN")),
            Arguments.of(CountryEntityDTO("    #d>1", 91, "IN")),
            Arguments.of(CountryEntityDTO("India", 1, "IN")),
            Arguments.of(CountryEntityDTO("India", 91, "1")),
            Arguments.of(CountryEntityDTO("India", 91, "##")),
            Arguments.of(CountryEntityDTO("India", 91, "  ##")),
            Arguments.of(CountryEntityDTO("India", 91, "  #4")),
        )

        @JvmStatic
        fun inValidCountryInput() = Stream.of(
            Arguments.of(CountryEntityDTO("", 91, "IN")),
            Arguments.of(CountryEntityDTO(" ", 91, "IN")),
            Arguments.of(CountryEntityDTO("sd", 91, "IN")),
            Arguments.of(CountryEntityDTO("12#", 91, "IN")),
            Arguments.of(CountryEntityDTO("India", 0, "IN")),
            Arguments.of(CountryEntityDTO("India", -1, "IN")),
            //Arguments.of(CountryEntityDTO("India", 91, " ")),
            Arguments.of(CountryEntityDTO("", 0, " "))
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

    @Test
    fun `Delete country by id`() {
        val inputCountry = CountryEntityDTO("India", 91, "IN")

        val insertedCountry = transaction {
            TableCountryEntity.new {
                name = inputCountry.name
                shortcode = inputCountry.shortCode
                isocode = inputCountry.callingCode
            }
        }

        assertEquals(inputCountry.name, insertedCountry.name)

        val deleteCountry = transaction {
            TableCountry.deleteWhere {
                TableCountry.id eq insertedCountry.id
            }
        }

        assertEquals(1, deleteCountry)
    }

    @Test
    fun `Failed to delete country by id as it didn't exist in db`() {

        val deleteCountry = transaction {
            TableCountry.deleteWhere {
                TableCountry.id eq 45
            }
        }

        assertEquals(0, deleteCountry)
    }

    @Test
    fun `Get country by id`() {
        val inputCountry = CountryEntityDTO("India", 91, "IN")

        val insertedCountry = transaction {
            TableCountryEntity.new {
                name = inputCountry.name
                shortcode = inputCountry.shortCode
                isocode = inputCountry.callingCode
            }
        }

        val foundCountry = transaction {
            TableCountryEntity.findById(insertedCountry.id)
        }

        assertNotNull(foundCountry?.isocode)
        assertEquals(inputCountry.name, foundCountry!!.name)
    }

    @Test
    fun `Failed to get country by id as it didn't exist in db`() {

        val foundCountry = transaction {
            TableCountryEntity.findById(45)
        }

        assertNull(foundCountry)
    }

    @Test
    fun `Get all inserted country`() {
        val countryInputList = listOf(
            CountryEntityDTO("India", 91, "IN"),
            CountryEntityDTO("United Arab Emirates", 971, "UAE")
        )

        countryInputList.forEach {
            val inputCountry: CountryEntityDTO = it

            transaction {
                TableCountryEntity.new {
                    name = inputCountry.name
                    shortcode = inputCountry.shortCode
                    isocode = inputCountry.callingCode
                }
            }
        }

        val countryList = transaction {
            TableCountryEntity.all().map {
                CountryModal.DTO(it.id.value, it.name, it.isocode, it.shortcode)
            }
        }
        assertEquals(countryInputList.size, countryList.size)
        assertEquals(countryInputList.first().name, countryList.first().name)
    }

    @ParameterizedTest
    @MethodSource("validCountryInput")
    fun `Failed to add, if shortCode, name, iso code is not unique`(
        inputCountry: CountryEntityDTO
    ) {
        transaction {
            TableCountryEntity.new {
                name = "India"
                shortcode = "IN"
                isocode = 91
            }
        }

        assertThrows<ExposedSQLException> {
            transaction {
                TableCountryEntity.new {
                    name = inputCountry.name
                    shortcode = inputCountry.shortCode
                    isocode = inputCountry.callingCode
                }
            }
        }
    }

    @ParameterizedTest
    @MethodSource("validUpdateCountryInput")
    fun `update country by id`(countryInfo: CountryEntityDTO) {
        val inputCountry = CountryEntityDTO("India", 91, "IN")

        val insertedCountry = transaction {
            TableCountryEntity.new {
                name = inputCountry.name
                shortcode = inputCountry.shortCode
                isocode = inputCountry.callingCode
            }
        }

        assertEquals(inputCountry.callingCode, insertedCountry.isocode)

        val updateCountry = transaction {

            TableCountry.update({ TableCountry.id eq insertedCountry.id }) {
                if (countryInfo.name.isNotBlank())
                    it[name] = countryInfo.name.trimStart()
                if (countryInfo.shortCode.isNotBlank())
                    it[shortcode] = countryInfo.shortCode.trimStart()
                if (0 < countryInfo.callingCode)
                    it[callingCode] = countryInfo.callingCode
            }
        }

        assertEquals(1, updateCountry)
    }

    @ParameterizedTest
    @MethodSource("inValidUpdateCountryInput")
    fun `failed to update country by id, due to invalid inputs`(countryInfo: CountryEntityDTO) {
        val inputCountry = CountryEntityDTO("India", 91, "IN")

        val insertedCountry = transaction {
            TableCountryEntity.new {
                name = inputCountry.name
                shortcode = inputCountry.shortCode
                isocode = inputCountry.callingCode
            }
        }

        assertEquals(inputCountry.callingCode, insertedCountry.isocode)

        assertThrows<ExposedSQLException> {
            transaction {
                TableCountry.update({ TableCountry.id eq insertedCountry.id }) {
                    if (countryInfo.name.isNotBlank())
                        it[name] = countryInfo.name.trimStart()
                    if (countryInfo.shortCode.isNotBlank())
                        it[shortcode] = countryInfo.shortCode.trimStart()
                    if (0 < countryInfo.callingCode)
                        it[callingCode] = countryInfo.callingCode
                }
            }
        }
    }

    @ParameterizedTest
    @MethodSource("validCountryInput")
    fun `Tests for the successfull insertion of valid country`(
        inputCountry: CountryEntityDTO
    ) {
        val insertedCountry = transaction {
            TableCountryEntity.new {
                name = inputCountry.name
                shortcode = inputCountry.shortCode
                isocode = inputCountry.callingCode
            }
        }

        val findInsertedCountry = transaction {
            TableCountryEntity.findById(insertedCountry.id)
        }

        assertNotNull(findInsertedCountry)
        assertEquals(inputCountry.name, findInsertedCountry!!.name)
    }

    @ParameterizedTest
    @MethodSource("inValidCountryInput")
    fun `Insertion failed to invalid country`(
        inputCountry: CountryEntityDTO
    ) {
        assertThrows<ExposedSQLException> {
            transaction {
                TableCountryEntity.new {
                    name = inputCountry.name
                    shortcode = inputCountry.shortCode
                    isocode = inputCountry.callingCode
                }
            }
        }
    }
}