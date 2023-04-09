package dev.reprator.splash

import io.ktor.server.config.*
import java.io.File
import java.io.IOException

fun ApplicationConfig?.setUpSplashFolder(): File? {
    this ?: return null
    val khataBookConfig = config("khatabook")

    val uploadDirPath: String = khataBookConfig.property("splash.dir").getString()
    val uploadDir = File(uploadDirPath)
    if (!uploadDir.mkdirs() && !uploadDir.exists()) {
        throw IOException("Failed to create directory ${uploadDir.absolutePath}")
    }
    return uploadDir
}