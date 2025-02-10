package no.ssb.metadata.vardef.models

import io.micronaut.serde.annotation.Serdeable
import io.micronaut.serde.config.naming.SnakeCaseStrategy
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotEmpty
import no.ssb.metadata.vardef.annotations.DaplaGroup
import no.ssb.metadata.vardef.annotations.DaplaTeam
import no.ssb.metadata.vardef.constants.KLASS_REFERENCE_SUBJECT_FIELD_EXAMPLE
import no.ssb.metadata.vardef.constants.OWNER_EXAMPLE
import no.ssb.metadata.vardef.constants.RENDERED_CONTACT_EXAMPLE

@Schema(examples = [KLASS_REFERENCE_SUBJECT_FIELD_EXAMPLE])
@Serdeable(naming = SnakeCaseStrategy::class)
data class KlassReference(
    val referenceUri: String,
    val code: String?,
    val title: String?,
)

@Serdeable(naming = SnakeCaseStrategy::class)
data class Contact(
    val title: LanguageStringType,
    @Email
    val email: String,
)

/**
 * Owner
 *
 * @property team The Dapla team with responsibility for this variable definition.
 * @property groups The groups with permission to modify this variable definition.
 *
 */
@Schema(example = OWNER_EXAMPLE)
@Serdeable(naming = SnakeCaseStrategy::class)
data class Owner(
    @NotEmpty
    @DaplaTeam
    val team: String,
    @NotEmpty
    val groups: List<
        @NotEmpty @DaplaGroup
        String,
    >,
)

@Schema(example = RENDERED_CONTACT_EXAMPLE)
@Serdeable(naming = SnakeCaseStrategy::class)
data class RenderedContact(
    val title: String?,
    val email: String,
)
