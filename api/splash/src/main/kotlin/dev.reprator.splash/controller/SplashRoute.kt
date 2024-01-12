package dev.reprator.splash.controller

import dev.reprator.base.beans.UPLOAD_FOLDER_SPLASH
import dev.reprator.base_ktor.util.respondWithResult
import dev.reprator.language.domain.LanguageFacade
import dev.reprator.splash.modal.SplashModal
import io.ktor.http.*
import io.ktor.server.routing.*
import kotlinx.coroutines.async
import org.koin.core.qualifier.named
import org.koin.ktor.ext.inject
import java.io.File
import java.io.IOException
import org.koin.ktor.ext.get as koinGet

const val ENDPOINT_SPLASH = "/splash"

fun Routing.routeSplash() {

    val splashDirectory by lazy { setUpSplashFolder(koinGet<String>(named(UPLOAD_FOLDER_SPLASH))) }

    val languageFacade by inject<LanguageFacade>()

    route(ENDPOINT_SPLASH) {
        get {

            val fileAsyncResult = async {
                splashDirectory.listFiles()?.map {
                    it.canonicalPath
                }.orEmpty()
            }

            val languageAsyncResult = async {
                languageFacade.getAllLanguage().toList()
            }

            val splashModal = SplashModal(fileAsyncResult.await(), languageAsyncResult.await())
            respondWithResult(HttpStatusCode.OK, splashModal)
        }

    }
}

private fun setUpSplashFolder(uploadDirPath: String): File {
    val uploadDir = File(uploadDirPath)
    if (!uploadDir.mkdirs() && !uploadDir.exists()) {
        throw IOException("Failed to create directory ${uploadDir.absolutePath}")
    }
    return uploadDir
}