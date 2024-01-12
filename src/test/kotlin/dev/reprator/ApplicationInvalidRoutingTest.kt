package dev.reprator

import dev.reprator.base.usecase.AppResult
import dev.reprator.commonFeatureImpl.di.koinAppCommonDBModule
import dev.reprator.commonFeatureImpl.di.koinAppCommonModule
import dev.reprator.commonFeatureImpl.di.koinAppNetworkClientModule
import dev.reprator.language.controller.ENDPOINT_LANGUAGE
import dev.reprator.splash.modal.SplashModal
import dev.reprator.testModule.KtorServerExtension
import dev.reprator.testModule.KtorServerExtension.Companion.TEST_SERVER
import dev.reprator.testModule.di.appTestCoreModule
import dev.reprator.testModule.di.appTestDBModule
import dev.reprator.testModule.hitApiWithClient
import io.ktor.http.*
import io.ktor.server.engine.*
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
internal class ApplicationInvalidRoutingTest: KoinTest {

    @JvmField
    @RegisterExtension
    val koinTestExtension = KoinTestExtension.create {

        modules(appTestCoreModule, koinAppCommonModule(TEST_SERVER!!.environment.config),
            koinAppDBModule, appTestDBModule, koinAppCommonDBModule, koinAppNetworkClientModule
        )
    }

    @Test
    fun `throw 404, if api doesn't exist`(): Unit = runBlocking {
        val response = hitApiWithClient<SplashModal>(getKoin(), "$ENDPOINT_LANGUAGE/InvalidApi", HttpMethod.Get) as AppResult.Error.HttpError

        Assertions.assertEquals(HttpStatusCode.NotFound.value, response.code)
        Assertions.assertNotNull(response.errorBody)
    }
}