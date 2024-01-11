package dev.reprator.testModule.di

import dev.reprator.core.usecase.JWTConfiguration
import dev.reprator.core.util.dbConfiguration.DatabaseFactory
import dev.reprator.core.util.logger.AppLogger
import impl.AppLoggerImpl
import impl.DatabaseFactoryImpl
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

const val JWT_PARAMETER_SECRET = "sdfrw35r3534"
const val JWT_PARAMETER_AUDIENCE = "JWTaudience"
const val JWT_PARAMETER_ISSUER = "JWTissuer"
const val JWT_PARAMETER_REALM = "realm"

const val JWT_SERVICE = "jwtRealmTest"

var TOKEN_ACCESS: String = ""
var TOKEN_REFRESH: String = ""

@Module
class AppCoreModule {

    @Single
    fun provideJWTConfiguration(): JWTConfiguration {
        return JWTConfiguration(
            JWT_PARAMETER_SECRET, JWT_PARAMETER_AUDIENCE, JWT_PARAMETER_ISSUER,
            JWT_PARAMETER_REALM
        )
    }

    @Single
    fun provideAppLogger(): AppLogger {
        return AppLoggerImpl()
    }

    @Single
    fun provideDatabaseFactory(): DatabaseFactory {
        return DatabaseFactoryImpl()
    }
}