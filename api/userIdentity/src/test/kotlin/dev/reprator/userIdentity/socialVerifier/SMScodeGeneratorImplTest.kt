package dev.reprator.userIdentity.socialVerifier

import dev.reprator.testModule.KtorServerExtension
import dev.reprator.testModule.MockClientResponseHandler
import dev.reprator.testModule.koinAppTestNetworkModule
import io.ktor.client.*
import io.ktor.util.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.inject
import org.koin.test.junit5.KoinTestExtension

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(KtorServerExtension::class)
internal class SMScodeGeneratorImplTest : KoinTest {

    private val httpClient by inject<HttpClient>()

    @JvmField
    @RegisterExtension
    val koinTestExtension = KoinTestExtension.create {

        modules(koinAppTestNetworkModule, module {
            single<List<MockClientResponseHandler>>{
                listOf(SmsResponseHandler())
            }
        })
    }

    @AfterEach
    fun closeDataBase() {
        httpClient.close()
    }

    @Test
    fun `failed to send the code to mobile number, due to invalid number`() = runBlocking {
        val sMScodeGenerator = SMScodeGeneratorTestFailImpl(httpClient, get<Attributes>())
        val posts = sMScodeGenerator.sendOtpToMobileNumber(0, "2432", 123456)
        Assertions.assertEquals(false, posts)
    }

    @Test
    fun `Successfully sent the code to mobile number`() = runBlocking {
        val sMScodeGenerator = SMScodeGeneratorTestSuccessImpl(httpClient, get<Attributes>())
        val posts = sMScodeGenerator.sendOtpToMobileNumber(91, "9041866044", 123456)
        Assertions.assertEquals(true, posts)
    }
}