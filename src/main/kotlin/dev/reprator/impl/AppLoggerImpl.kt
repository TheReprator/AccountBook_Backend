package dev.reprator.impl

import dev.reprator.core.util.logger.AppLogger

class AppLoggerImpl : AppLogger {

    enum class AppLogLevel {
        Verbose,
        Debug,
        Info,
        Warn,
        Error
    }

    private val tag: String
        get() = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).callerClass.name


    private fun log(
        severity: AppLogLevel,
        tag: String = this.tag,
        throwable: Throwable?,
        message: String
    ) {
        println(formatMessage(severity, message, tag, throwable))

    }

    private fun formatMessage(severity: AppLogLevel, message: String, tag: String, throwable: Throwable?): String = "$severity: ($tag) $message"

    private fun processLog(
        severity: AppLogLevel,
        tag: String,
        throwable: Throwable?,
        message: String
    ) {
        println(formatMessage(severity, message, tag, throwable))
        throwable?.printStackTrace()
    }

    override fun v(throwable: Throwable?, message: () -> String) {
        log(AppLogLevel.Verbose, tag, throwable, message())
    }

    override fun d(throwable: Throwable?, message: () -> String) {
        log(AppLogLevel.Debug, tag, throwable, message())
    }

    override fun i(throwable: Throwable?, message: () -> String) {
        log(AppLogLevel.Info, tag, throwable, message())
    }

    override fun e(throwable: Throwable?, message: () -> String) {
        log(AppLogLevel.Error, tag, throwable, message())
    }

    override fun w(throwable: Throwable?, message: () -> String) {
        log(AppLogLevel.Warn, tag, throwable, message())
    }

}