package no.ssb.metadata.vardef

import io.micronaut.runtime.Micronaut
import io.swagger.v3.oas.annotations.ExternalDocumentation
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.extensions.Extension
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty
import io.swagger.v3.oas.annotations.info.Contact
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.info.License
import io.swagger.v3.oas.annotations.security.SecurityScheme
import io.swagger.v3.oas.annotations.servers.Server
import io.swagger.v3.oas.annotations.tags.Tag
import no.ssb.metadata.vardef.constants.*

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
    servers = [Server(url = "https://metadata.intern.test.ssb.no", description = "Internal test server")],
    tags = [
        Tag(
            name = PUBLIC,
            description = "Operations which are available to the general public without authentication.",
        ),
        Tag(
            name = VALIDITY_PERIODS,
            description = "Create and access validity periods.",
        ),
        Tag(
            name = PATCHES,
            description = "Create and access 'patch' changes.",
        ),
        Tag(
            name = DATA_MIGRATION,
            description = "Create variable definitions from existing definitions in Vardok.",
            externalDocs =
                ExternalDocumentation(
                    description = "Vardok website",
                    url = "https://www.ssb.no/a/metadata/definisjoner/variabler/main.html",
                ),
        ),
        Tag(
            name = DRAFT,
            description = "Create, update and delete variable definitions with DRAFT status.",
        ),
    ],
)
@SecurityScheme(
    name = "Bearer Authentication",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    scheme = "bearer",
)
object Api

fun main(args: Array<String>) {
    Micronaut
        .build(*args)
        .deduceCloudEnvironment(true)
        .start()
}
