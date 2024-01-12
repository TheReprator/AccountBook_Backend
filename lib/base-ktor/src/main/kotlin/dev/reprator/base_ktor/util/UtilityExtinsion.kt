import io.ktor.server.config.*

fun ApplicationConfig.propertyConfig(property: String): String {
    return this.property(property).getString()
}