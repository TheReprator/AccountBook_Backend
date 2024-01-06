package dev.reprator.splash.controller

import dev.reprator.core.usecase.ResultDTOResponse
import dev.reprator.core.util.constants.UPLOAD_FOLDER_SPLASH
import dev.reprator.language.domain.LanguageFacade
import dev.reprator.language.modal.LanguageModal
import dev.reprator.splash.modal.SplashModal
import dev.reprator.testModule.KtorServerExtension
import dev.reprator.testModule.KtorServerExtension.Companion.BASE_URL
import dev.reprator.testModule.createHttpClient
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.mockk.coEvery
import io.mockk.mockkClass
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.junit5.mock.MockProviderExtension
import org.koin.test.mock.declareMock

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(KtorServerExtension::class)
internal class SplashController : KoinTest {

    @JvmField
    @RegisterExtension
    val mockProvider = MockProviderExtension.create { clazz ->
        // Your way to build a Mock here
        mockkClass(clazz)
    }

    @Test
    fun `Fetch Splash api`(): Unit = runBlocking {

        startKoin {
            modules(
                module {
                factory(named(UPLOAD_FOLDER_SPLASH)) { "splashFileDirectory" }
            })
        }

        val mockLanguageFacade = declareMock<LanguageFacade>()

        val langList: List<LanguageModal> = listOf(LanguageModal.DTO(1, "Hindi"), LanguageModal.DTO(2, "Arabic"))

        coEvery {
            mockLanguageFacade.getAllLanguage()
        } returns langList


        val client = createHttpClient()
        val response: ResultDTOResponse<SplashModal> = client.get("$BASE_URL$ENDPOINT_SPLASH").body()

        Assertions.assertNotNull(response)
        Assertions.assertEquals(langList.size, response.data.languageList.size)
        Assertions.assertEquals(langList, response.data.languageList)
    }
}