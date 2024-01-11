package dev.reprator.di

import dev.reprator.core.usecase.JWTConfiguration
import dev.reprator.core.usecase.JwtTokenService
import dev.reprator.core.util.constants.APP_COROUTINE_SCOPE
import dev.reprator.core.util.constants.UPLOAD_FOLDER_SPLASH
import dev.reprator.core.util.constants.VERIFICATION_SMS_PHONE_APIKEY
import dev.reprator.core.util.constants.VERIFICATION_SMS_PHONE_USERID
import dev.reprator.core.util.dbConfiguration.DatabaseConfig
import dev.reprator.core.util.dbConfiguration.DatabaseFactory
import dev.reprator.core.util.logger.AppLogger
import dev.reprator.dao.DefaultDatabaseFactory
import dev.reprator.impl.AppLoggerImpl
import dev.reprator.impl.JwtTokenServiceImpl
import dev.reprator.userIdentity.data.UserIdentityRepository
import io.ktor.server.application.*
import io.ktor.server.config.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Module
class AppModule constructor(private val applicationEnvironment: ApplicationEnvironment) {

    private val propertyConfig: ApplicationConfig.(String) -> String = {
        this.property(it).getString()
    }

    @Single
    fun provideAppLogger(): AppLogger {
        return AppLoggerImpl()
    }

    @Single
    @Named(APP_COROUTINE_SCOPE)
    fun provideCoroutineScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }

    @Factory
    fun provideApplicationConfig(): ApplicationConfig = applicationEnvironment.config

    @Factory
    @Named(UPLOAD_FOLDER_SPLASH)
    fun provideSplashFolder(config: ApplicationConfig): String = config.propertyConfig(UPLOAD_FOLDER_SPLASH)

    @Factory
    @Named(VERIFICATION_SMS_PHONE_APIKEY)
    fun provideSMSApiKey(config: ApplicationConfig): String = config.propertyConfig(VERIFICATION_SMS_PHONE_APIKEY)

    @Factory
    @Named(VERIFICATION_SMS_PHONE_USERID)
    fun provideSMSUserId(config: ApplicationConfig): String = config.propertyConfig(VERIFICATION_SMS_PHONE_USERID)

    @Single
    fun provideJwtTokenService(
        config: ApplicationConfig
    ): JWTConfiguration {
        fun property(property: String): String = config.propertyConfig("jwt.$property")
        return JWTConfiguration(property("secret"), property("audience"), property("issuer"), property("realm"))
    }

    @Single
    fun provideDatabaseFactory(config: ApplicationConfig,): DatabaseFactory {
        val property:(property: String) -> String = {
            config.propertyConfig("storage.$it")
        }
        return DefaultDatabaseFactory(
            (DatabaseConfig(
                property("driverClassName"), property("databaseName"),
                property("portNumber").toInt(), property("serverName"), property("userName"), property("password")
            ))
        )
    }
}