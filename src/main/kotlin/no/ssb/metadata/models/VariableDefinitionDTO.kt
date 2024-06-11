package no.ssb.metadata.models

import io.micronaut.serde.annotation.Serdeable
import io.micronaut.serde.config.naming.SnakeCaseStrategy
import io.swagger.v3.oas.annotations.media.Schema
import no.ssb.metadata.constants.DEFINITION_FIELD_DESCRIPTION
import no.ssb.metadata.constants.ID_FIELD_DESCRIPTION
import no.ssb.metadata.constants.NAME_FIELD_DESCRIPTION
import no.ssb.metadata.constants.SHORT_NAME_FIELD_DESCRIPTION

@Serdeable(naming = SnakeCaseStrategy::class)
@Schema(
    description = """Variable definition Data Transfer Object.

        Used for reading a Variable Definition. The Variable Definition will be rendered in the chosen language.
    """,
    // The example should be as realistic as possible for the variable Landbakgrunn, rendered in Norwegian Bokmål.
    example = """
        {
            "id": "i03qoh1e",
            "name": "Landbakgrunn",
            "short_name": "landbak",
            "definition": "For personer født i utlandet, er dette (med noen få unntak) eget fødeland. For personer født i Norge er det foreldrenes fødeland. I de tilfeller der foreldrene har ulikt fødeland, er det morens fødeland som blir valgt. Hvis ikke personen selv eller noen av foreldrene er utenlandsfødt, hentes landbakgrunn fra de første utenlandsfødte en treffer på i rekkefølgen mormor, morfar, farmor eller farfar."
        }
    """,
)
data class VariableDefinitionDTO(
    @Schema(description = ID_FIELD_DESCRIPTION)
    val id: String?,
    @Schema(description = NAME_FIELD_DESCRIPTION)
    val name: String?,
    @Schema(description = SHORT_NAME_FIELD_DESCRIPTION)
    val shortName: String?,
    @Schema(description = DEFINITION_FIELD_DESCRIPTION)
    val definition: String?,
)
