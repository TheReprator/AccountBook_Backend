package dev.reprator.language.controller

import dev.reprator.language.domain.IllegalLanguageException
import dev.reprator.language.domain.LanguageFacadeImplTest
import dev.reprator.language.domain.LanguageFacadeImplTest.Companion.LANGUAGE_LIST
import dev.reprator.language.domain.LanguageNotFoundException
import dev.reprator.language.modal.LanguageEntity
import dev.reprator.language.modal.LanguageModal
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LanguageControllerTest {

    private lateinit var languageController: LanguageController

    @BeforeEach
    fun setUpLanguageController() {
        languageController = LanguageControllerImpl(LanguageFacadeImplTest())

        LANGUAGE_LIST.clear()
        val langList = mutableListOf<LanguageModal>(LanguageModal.DTO(1, "English"), LanguageModal.DTO(2, "Hindi"))
        LANGUAGE_LIST.addAll(langList)
    }

    @Test
    fun `Get all language from list`(): Unit = runBlocking {
        val response = languageController.getAllLanguage()

        assertNotNull(response)
        assertEquals(LANGUAGE_LIST, response)
    }

    @Test
    fun `Get language by id from list`(): Unit = runBlocking {
        val languageId = 1
        val response = languageController.getLanguage(languageId)

        assertNotNull(response)
        assertEquals(languageId, response.id)
    }

    @Test
    fun `Failed to Get language by id from list, as it didn't exist`(): Unit = runBlocking {
        val languageId = 10

        assertThrows<LanguageNotFoundException> {
            languageController.getLanguage(languageId)
        }
    }

    @Test
    fun `Add new language, as it didn't exist in list`(): Unit = runBlocking {
        val inputLanguage = "Arabic"

        val response = languageController.addNewLanguage(inputLanguage)

        assertEquals(inputLanguage, response.name)
    }

    @Test
    fun `Failed to Add new language, as it exist in list`(): Unit = runBlocking {
        val inputLanguage = "English"

        assertThrows<IllegalLanguageException> {
            languageController.addNewLanguage(inputLanguage)
        }
    }

    @Test
    fun `Edit language, as it exist in list`(): Unit = runBlocking {
        val inputLanguage = LanguageEntity.DTO(1, "Chinese")

        val response = languageController.editLanguage(inputLanguage.id, inputLanguage)

        assertNotNull(response)
        assertEquals(true, response)
    }

    @Test
    fun `Failed to Edit a language, as it didn't exist in list`(): Unit = runBlocking {
        val inputLanguage = LanguageEntity.DTO(12, "Arabic")

        assertThrows<IllegalLanguageException> {
            languageController.editLanguage(inputLanguage.id, inputLanguage)
        }
    }

    @Test
    fun `Delete language, as it exist in list`(): Unit = runBlocking {
        val inputLanguage = 1

        val response = languageController.deleteLanguage(inputLanguage)

        assertNotNull(response)
        assertEquals(true, response)
    }

    @Test
    fun `Failed to Delete a language, as it didn't exist in list`(): Unit = runBlocking {
        val inputLanguage = 13

        assertThrows<IllegalLanguageException> {
            languageController.deleteLanguage(inputLanguage)
        }
    }
}