package no.ssb.metadata.vardef

import io.micronaut.health.HealthStatus
import io.micronaut.management.health.indicator.HealthIndicator
import io.micronaut.management.health.indicator.HealthResult
import io.micronaut.management.health.indicator.annotation.Liveness
import io.micronaut.openapi.annotation.OpenAPIGroupInfo
import io.micronaut.runtime.Micronaut
import io.swagger.v3.oas.annotations.ExternalDocumentation
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.Contact
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.info.License
import io.swagger.v3.oas.annotations.security.SecurityScheme
import io.swagger.v3.oas.annotations.servers.Server
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.inject.Singleton
import no.ssb.metadata.vardef.constants.*
import org.reactivestreams.Publisher
import reactor.core.publisher.Mono

@Suppress("ktlint:standard:max-line-length")
@OpenAPIGroupInfo(
    names = ["public"],
    info =
        OpenAPIDefinition(
            info =
                Info(
                    title = "Public Variable Definitions API",
                    description = """Public Variable Definitions""",
                    version = "0.1",
                    license = License(name = "CC BY 4.0", url = "https://creativecommons.org/licenses/by/4.0/deed.no"),
                    contact = Contact(email = "metadata@ssb.no", name = "Team Metadata"),
                ),
            servers = [
                Server(url = "https://metadata.ssb.no", description = "Public server"),
                Server(url = "https://metadata.test.ssb.no", description = "Public test server"),
                Server(url = "http://localhost:8080", description = "Local development"),
            ],
        ),
)
@OpenAPIGroupInfo(
    names = ["internal"],
    info =
        OpenAPIDefinition(
            info =
                Info(
                    title = "Internal Variable Definitions Administration API",
                    description = """
## Introduction

Variable Definitions are centralized definitions of concrete variables which are typically present in multiple datasets. Variable Definitions support standardization of data and metadata and facilitate sharing and joining of data by clarifying when variables have an identical definition.

## Maintenance of Variable Definitions
This API allows for creation, maintenance and access of Variable Definitions.

### Ownership
Creation and maintenance of variables may only be performed by Statistics Norway employees representing a specific Dapla team, who are defined as the owners of a given Variable Definition. The team an owner represents must be specified when making a request through the `active_group` query parameter. All maintenance is to be performed by the owners, with no intervention from administrators.

### Status
All Variable Definitions have an associated status. The possible values for status are `DRAFT`, `PUBLISHED_INTERNAL` and `PUBLISHED_EXTERNAL`.

#### Draft
When a Variable Definition is created it is assigned the status `DRAFT`. Under this status the Variable Definition is:

- Only visible to Statistics Norway employees.
- Mutable (it may be changed directly without need for versioning).
- Not suitable to refer to from other systems.

This status may be changed to `PUBLISHED_INTERNAL` or `PUBLISHED_EXTERNAL` with a direct update.

#### Published Internal
Under this status the Variable Definition is:

- Only visible to Statistics Norway employees.
- Immutable (all changes are versioned).
- Suitable to refer to in internal systems for statistics production.
- Not suitable to refer to for external use (for example in Statistikkbanken).

This status may be changed to `PUBLISHED_EXTERNAL` by creating a Patch version.

#### Published External
Under this status the Variable Definition is:

- Visible to the general public.
- Immutable (all changes are versioned).
- Suitable to refer to from any system.

This status may not be changed as it would break immutability. If a Variable Definition is no longer relevant then its period of validity should be ended by specifying a `valid_until` date in a Patch version.

### Immutability
Variable Definitions are immutable. This means that any changes must be performed in a strict versioning system. Consumers can avoid being exposed to breaking changes by specifying a `date_of_validity` when they request a Variable Definition.

#### Patches
Patches are for changes which do not affect the fundamental meaning of the Variable Definition.

#### Validity Periods
Validity Periods are versions with a period defined by a `valid_from` date and optionally a `valid_until` date. If the fundamental meaning of a Variable Definition is to be changed, it should be done by creating a new Validity Period.

""",
                    version = "0.1",
                    license = License(name = "CC BY 4.0", url = "https://creativecommons.org/licenses/by/4.0/deed.no"),
                    contact = Contact(email = "metadata@ssb.no", name = "Team Metadata"),
                ),
            servers = [
                Server(
                    url = "https://metadata.intern.ssb.no",
                    description = "Internal server",
                ),
                Server(
                    url = "https://metadata.intern.test.ssb.no",
                    description = "Internal test server",
                ),
                Server(url = "http://localhost:8080", description = "Local development"),
            ],
            tags = [
                Tag(name = VALIDITY_PERIODS, description = "Create and access Validity Periods."),
                Tag(
                    name = PATCHES,
                    description = "Create and access Patches.",
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
                Tag(name = DRAFT, description = "Create, update and delete variable definitions with DRAFT status."),
            ],
        ),
    securitySchemes = [
        SecurityScheme(
            name = KEYCLOAK_TOKEN_SCHEME,
            description =
                "A token granted by Statistics Norway's Keycloak instance. May be obtained " +
                    "from a <a href=https://lab.dapla.ssb.no>Dapla Lab</a> service.",
            type = SecuritySchemeType.HTTP,
            bearerFormat = "JWT",
            scheme = "bearer",
        ),
    ],
)
object Api

@Singleton
@Liveness
class LivenessIndicator : HealthIndicator {
    override fun getResult(): Publisher<HealthResult> = Mono.just(HealthResult.builder(LIVENESS_NAME).status(HealthStatus.UP).build())

    companion object {
        private const val LIVENESS_NAME = "liveness"
    }
}

fun main(args: Array<String>) {
    Micronaut
        .build(*args)
        .deduceCloudEnvironment(true)
        .start()
}
