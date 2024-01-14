package dev.reprator

import dev.reprator.base.usecase.AppResult
import dev.reprator.commonFeatureImpl.di.koinAppCommonDBModule
import dev.reprator.commonFeatureImpl.di.koinAppCommonModule
import dev.reprator.commonFeatureImpl.di.koinAppNetworkClientModule
import dev.reprator.splash.modal.SplashModal
import dev.reprator.testModule.KtorServerExtension
import dev.reprator.testModule.KtorServerExtension.Companion.TEST_SERVER
import dev.reprator.testModule.di.appTestCoreModule
import dev.reprator.testModule.di.appTestDBModule
import dev.reprator.testModule.hitApiWithClient
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.test.KoinTest
import org.koin.test.junit5.KoinTestExtension

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(KtorServerExtension::class)
internal class ApplicationInvalidRoutingTest : KoinTest {

    @JvmField
    @RegisterExtension
    val koinTestExtension = KoinTestExtension.create {
        modules(
            listOf(
                appTestCoreModule, koinAppCommonModule(TEST_SERVER!!.environment.config),
                appTestDBModule{ _, _ ->

                }, koinAppCommonDBModule, koinAppNetworkClientModule
            )
        )

        TEST_SERVER!!.application.module()
    }

    @Test
    fun `throw 404, if api doesn't exist`(): Unit = runBlocking {

        val response =
            hitApiWithClient<SplashModal>(getKoin(), "InvalidApi", HttpMethod.Get) as AppResult.Error.HttpError

        Assertions.assertEquals(HttpStatusCode.NotFound.value, response.code)
        Assertions.assertNotNull(response.errorBody)
    }

    @Test
    fun `shut down server`(): Unit = runBlocking {

        val response =
            hitApiWithClient<Unit>(getKoin(), "appShutDown", HttpMethod.Get) as AppResult.Error.HttpError

        Assertions.assertEquals(HttpStatusCode.Gone.value, response.code)
    }
}