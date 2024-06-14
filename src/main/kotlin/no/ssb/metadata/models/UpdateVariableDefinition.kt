package no.ssb.metadata.models

import io.micronaut.core.annotation.Nullable
import io.micronaut.serde.annotation.Serdeable
import io.micronaut.serde.config.naming.SnakeCaseStrategy
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Pattern
import no.ssb.metadata.constants.*

/**
 * Update variable definition
 *
 * Data structure with all fields optional for updating an existing variable definition.
 */
@Serdeable(naming = SnakeCaseStrategy::class)
@Schema(
    example = """
        {
            "name":
                {   "en": "Country Background",
                    "nb": "Landbakgrunn",
                    "nn": "Landbakgrunn"
                },
            "short_name": "landbak",
            "definition":
                {
                    "en": "Country background is the person's own, the mother's or possibly the father's country of birth. Persons without an immigrant background always have Norway as country background. In cases where the parents have different countries of birth the mother's country of birth is chosen. If neither the person nor the parents are born abroad, country background is chosen from the first person born abroad in the order mother's mother, mother's father, father's mother, father's father.",
                    "nb": "For personer født i utlandet, er dette (med noen få unntak) eget fødeland. For personer født i Norge er det foreldrenes fødeland. I de tilfeller der foreldrene har ulikt fødeland, er det morens fødeland som blir valgt. Hvis ikke personen selv eller noen av foreldrene er utenlandsfødt, hentes landbakgrunn fra de første utenlandsfødte en treffer på i rekkefølgen mormor, morfar, farmor eller farfar.",
                    "nn": "For personar fødd i utlandet, er dette (med nokre få unntak) eige fødeland. For personar fødd i Noreg er det fødelandet til foreldra. I dei tilfella der foreldra har ulikt fødeland, er det fødelandet til mora som blir valt. Viss ikkje personen sjølv eller nokon av foreldra er utenlandsfødt, blir henta landsbakgrunn frå dei første utenlandsfødte ein treffar på i rekkjefølgja mormor, morfar, farmor eller farfar."
                }
            "classification_reference": "91",
            "unit_types": ["01", "02"],
            "subject_fields": ["he04"],
            "contains_unit_identifying_information": true,
            "contains_sensitive_personal_information": true,
            "variable_status": "Draft",
            "measurement_type": "volume",
            "valid_from": "2024-06-05",
            "valid_until": "2024-06-05",
            "external_reference_uri": "https://example.com/",
            "relevant_variable_definition_uri": [
                "https://example.com/"
            ],
            "contact": {
                "title": "",
                "email": ""
            }
        }
    """,
)
data class UpdateVariableDefinition(
    @Nullable
    @Schema(description = NAME_FIELD_DESCRIPTION)
    val name: LanguageStringType?,
    @Nullable
    @Schema(description = SHORT_NAME_FIELD_DESCRIPTION)
    @Pattern(regexp = "^[a-z0-9_]{3,}$")
    val shortName: String?,
    @Nullable
    @Schema(description = DEFINITION_FIELD_DESCRIPTION)
    val definition: LanguageStringType?,
    @Nullable
    @Schema(description = CLASSIFICATION_REFERENCE_FIELD_DESCRIPTION)
    @Pattern(regexp = "^[0-9]+$")
    val classificationReference: String?,
    @Nullable
    @Schema(description = UNIT_TYPES_FIELD_DESCRIPTION)
    val unitTypes: List<String>?,
    @Nullable
    val subjectFields: List<String>?,
    @Nullable
    val containsUnitIdentifyingInformation: Boolean?,
    @Nullable
    val containsSensitivePersonalInformation: Boolean?,
    @Nullable
    val variableStatus: String?,
    @Nullable
    val measurementType: String?,
    @Nullable
    @Pattern(regexp = "^\\d{4}-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])$")
    val validFrom: String?,
    @Nullable
    @Pattern(regexp = "^\\d{4}-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])$")
    val validUntil: String?,
    @Nullable
    @Pattern(regexp = "^(https?|ftp)://[^\\s/$.?#].\\S*$")
    val externalReferenceUri: String?,
    @Nullable
    val relatedVariableDefinitionUris: List<String>?,
    @Nullable
    val contact: Contact?,
)
