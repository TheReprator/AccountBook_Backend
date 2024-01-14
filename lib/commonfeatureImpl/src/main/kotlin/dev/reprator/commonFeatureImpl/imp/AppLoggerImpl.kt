package dev.reprator.commonFeatureImpl.imp

import dev.reprator.base.action.AppLogger
import java.util.regex.Pattern

class AppLoggerImpl : AppLogger {

    companion object {
        private val ANONYMOUS_CLASS = Pattern.compile("(\\$\\d+)+$")
    }

    enum class AppLogLevel {
        Verbose,
        Debug,
        Info,
        Warn,
        Error
    }

    private val tag: String
        get() = Throwable().stackTrace[3]
            .let(::createStackElementTag)

    /**
     * Extract the tag which should be used for the message from the `element`. By default
     * this will use the class name without any anonymous class suffixes (e.g., `Foo$1`
     * becomes `Foo`).
     *
     * Note: This will not be called if a [manual tag][.tag] was specified.
     */
    private fun createStackElementTag(element: StackTraceElement): String {
        var tag = element.fileName ?: element.className.substringAfterLast('.')
        val m = ANONYMOUS_CLASS.matcher(tag)
        if (m.find()) {
            tag = m.replaceAll("")
        }
        return "$tag:(${element.lineNumber})"
    }

    private fun log(
        severity: AppLogLevel,
        tag: String? = this.tag,
        throwable: Throwable?,
        message: String
    ) {
        println(formatMessage(severity, message, tag, throwable))
    }

    private fun formatMessage(severity: AppLogLevel, message: String, tag: String?, throwable: Throwable?): String =
        "$severity: ($tag) $message"

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