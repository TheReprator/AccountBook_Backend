package dev.reprator

import dev.reprator.core.APP_COROUTINE_SCOPE
import dev.reprator.core.AppLogger
import dev.reprator.core.DatabaseFactory
import dev.reprator.dao.DefaultDatabaseFactory
import dev.reprator.impl.LoggerImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.dsl.withOptions
import org.koin.core.qualifier.named
import org.koin.dsl.module


val koinAppModule = module {
    single<DatabaseFactory> { params -> DefaultDatabaseFactory(appConfig = params.get()) }
    single<AppLogger> { LoggerImpl() }
    single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.IO) }.withOptions { qualifier = named(APP_COROUTINE_SCOPE) }
}
