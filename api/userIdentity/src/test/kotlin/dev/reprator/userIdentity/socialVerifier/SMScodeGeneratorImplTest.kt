package dev.reprator.userIdentity.socialVerifier

import dev.reprator.core.util.logger.AppLogger
import dev.reprator.country.di.CountryComponent
import dev.reprator.testModule.KtorServerExtension
import dev.reprator.testModule.di.AppCoreComponent
import dev.reprator.testModule.plugin.errorResponse
import dev.reprator.userIdentity.di.UserdentityComponent
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.mock.*
import io.ktor.util.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.dsl.module
import org.koin.ksp.generated.module
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.junit5.KoinTestExtension

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(KtorServerExtension::class)
internal class SMScodeGeneratorImplTest : KoinTest {


    @JvmField
    @RegisterExtension
    val koinTestExtension = KoinTestExtension.create {
        modules( CountryComponent().module, UserdentityComponent().module, AppCoreComponent().module)

        val engine = MockEngine { request ->

            val handlerList = listOf(SmsResponseHandler())

            handlerList.forEach { handler ->
                val response = handler.handleRequest(this, request)
                if (response != null) {
                    return@MockEngine response
                }
            }
            return@MockEngine errorResponse()
        }

        modules(module {
            single<HttpClientEngine> { engine }
        })
    }

    @Test
    fun `failed to send the code to mobile number, due to invalid number`() = runBlocking {
        val sMScodeGenerator = SMScodeGeneratorClientTestFailImpl(get<HttpClient>(), get<Attributes>(), get<AppLogger>())
        val posts = sMScodeGenerator.sendOtpToMobileNumber(0, "2432", 123456)
        Assertions.assertEquals(false, posts)
    }

    @Test
    fun `Successfully sent the code to mobile number`() = runBlocking {
        val sMScodeGenerator = SMScodeGeneratorClientTestSuccessImpl(get<HttpClient>(), get<Attributes>(), get<AppLogger>())
        val posts = sMScodeGenerator.sendOtpToMobileNumber(91, "9041866044", 123456)
        Assertions.assertEquals(true, posts)
    }
}