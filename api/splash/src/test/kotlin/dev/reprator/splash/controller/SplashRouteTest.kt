package dev.reprator.splash.controller

import dev.reprator.base.beans.UPLOAD_FOLDER_SPLASH
import dev.reprator.base.usecase.AppResult
import dev.reprator.commonFeatureImpl.di.koinAppCommonDBModule
import dev.reprator.commonFeatureImpl.di.koinAppCommonModule
import dev.reprator.commonFeatureImpl.di.koinAppNetworkClientModule
import dev.reprator.language.domain.LanguageFacade
import dev.reprator.language.modal.LanguageModal
import dev.reprator.splash.modal.SplashModal
import dev.reprator.splash.module
import dev.reprator.testModule.KtorServerExtension
import dev.reprator.testModule.di.SchemaDefinition
import dev.reprator.testModule.di.appTestCoreModule
import dev.reprator.testModule.di.appTestDBModule
import dev.reprator.testModule.hitApiWithClient
import io.ktor.http.*
import io.mockk.coEvery
import io.mockk.mockkClass
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.junit5.KoinTestExtension
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

    private val ownModule = module {
        factory(named(UPLOAD_FOLDER_SPLASH)) { "../../splashFileDirectory" }
    }

    @JvmField
    @RegisterExtension
    val koinTestExtension = KoinTestExtension.create {

        modules(
            koinAppNetworkClientModule,
            koinAppCommonModule(KtorServerExtension.TEST_SERVER!!.environment.config),
            appTestCoreModule,
            appTestDBModule { hikariDataSource, _ ->
                SchemaDefinition.createSchema(hikariDataSource)
            },
            koinAppCommonDBModule, ownModule
        )

        KtorServerExtension.TEST_SERVER!!.application.module()
    }

    @Test
    fun `Fetch Splash api`(): Unit = runBlocking {

        val mockLanguageFacade = declareMock<LanguageFacade>()

        val langList: List<LanguageModal> = listOf(LanguageModal.DTO(1, "Hindi"), LanguageModal.DTO(2, "Arabic"))

        coEvery {
            mockLanguageFacade.getAllLanguage()
        } returns langList


        val response = hitApiWithClient<SplashModal>(getKoin(), ENDPOINT_SPLASH, HttpMethod.Get) as AppResult.Success

        Assertions.assertNotNull(response)
        Assertions.assertEquals(langList.size, response.body.languageList.size)
        Assertions.assertEquals(langList, response.body.languageList)
    }
}