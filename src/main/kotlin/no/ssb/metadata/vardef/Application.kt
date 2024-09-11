package no.ssb.metadata.vardef

import io.micronaut.runtime.Micronaut
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.extensions.Extension
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty
import io.swagger.v3.oas.annotations.info.Contact
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.info.License
import io.swagger.v3.oas.annotations.security.SecurityRequirement

@OpenAPIDefinition(
    info =
        Info(
            title = "Variable Definitions",
            description = "",
            version = "0.1",
            license = License(name = "CC BY 4.0", url = "https://creativecommons.org/licenses/by/4.0/deed.no"),
            contact = Contact(email = "metadata@ssb.no", name = "Team Metadata"),
            extensions =
                arrayOf(
                    Extension(
                        properties =
                            arrayOf(
                                ExtensionProperty(name = "x-audience", value = "external-public"),
                            ),
                    ),
                ),
        ),
    security = [SecurityRequirement(name = "Keycloak token")],
)
object Api

fun main(args: Array<String>) {
    Micronaut
        .build(*args)
        .deduceCloudEnvironment(true)
        .start()
}
