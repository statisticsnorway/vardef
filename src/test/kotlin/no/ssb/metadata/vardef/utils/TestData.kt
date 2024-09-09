package no.ssb.metadata.vardef.utils

import io.viascom.nanoid.NanoId
import no.ssb.metadata.vardef.models.*
import org.bson.types.ObjectId
import java.net.URI
import java.time.LocalDate
import java.time.LocalDateTime

val INPUT_VARIABLE_DEFINITION =
    InputVariableDefinition(
        id = NanoId.generate(8),
        name =
            LanguageStringType(
                nb = "Fagstillinger",
                nn = "Fagstillinger",
                en = "Specialised positions",
            ),
        shortName = "fagu",
        definition =
            LanguageStringType(
                nb =
                    "Angir ubesatte fagstillinger i barneverntjenesten per 31.12. " +
                        "En stilling blir definert som ledig dersom den har vært ubesatt fire måneder eller lengre.",
                nn = null,
                en =
                    "Indicates the vacant specialised positions in the child welfare service as per 31 December. " +
                        "A position is defined as vacant if it has been unfilled for four months or longer.",
            ),
        classificationReference = "91",
        unitTypes = listOf("", ""),
        subjectFields = listOf("", ""),
        containsSensitivePersonalInformation = false,
        variableStatus = VariableStatus.DRAFT,
        measurementType = "",
        validFrom = LocalDate.of(2021, 1, 1),
        validUntil = LocalDate.of(2021, 1, 1),
        externalReferenceUri = URI("https://www.example.com").toURL(),
        relatedVariableDefinitionUris = listOf(URI("https://www.example.com").toURL()),
        contact =
            Contact(
                LanguageStringType("", "", ""),
                "",
            ),
    )

val SAVED_VARIABLE_DEFINITION =
    SavedVariableDefinition(
        id = ObjectId(),
        definitionId = NanoId.generate(8),
        patchId = 1,
        name =
            LanguageStringType(
                nb = "Landbakgrunn",
                nn = "Landbakgrunn",
                en = "Country Background",
            ),
        shortName = "landbak",
        definition =
            LanguageStringType(
                nb = "For personer født",
                nn = "For personer født",
                en = "Country background is",
            ),
        classificationUri = "91",
        unitTypes = listOf("01", "02"),
        subjectFields = listOf("he04"),
        containsSensitivePersonalInformation = false,
        variableStatus = VariableStatus.PUBLISHED_EXTERNAL,
        measurementType = "02.01",
        validFrom = LocalDate.of(2021, 1, 1),
        validUntil = null,
        externalReferenceUri = URI("https://example.com/").toURL(),
        relatedVariableDefinitionUris = listOf(),
        owner =
            Owner("", ""),
        contact =
            Contact(
                LanguageStringType("", "", ""),
                "me@example.com",
            ),
        createdAt = LocalDateTime.parse("2024-06-11T08:15:19"),
        createdBy =
            Person("", ""),
        lastUpdatedAt = LocalDateTime.parse("2024-06-11T08:15:19"),
        lastUpdatedBy =
            Person("", ""),
    )

val SAVED_DRAFT_VARIABLE_DEFINITION =
    SAVED_VARIABLE_DEFINITION.copy(
        id = ObjectId(),
        definitionId = NanoId.generate(8),
        shortName = "ETASJENR",
        variableStatus = VariableStatus.DRAFT,
    )

val SAVED_VARIABLE_DEFINITION_COPY =
    SAVED_VARIABLE_DEFINITION.copy(
        id = ObjectId(),
        definitionId = NanoId.generate(8),
        name =
            LanguageStringType(
                nb = "Rolletype",
                nn = "Rolletype",
                en = "Role type",
            ),
        shortName = "Rolletype",
    )

val RENDERED_VARIABLE_DEFINITION =
    RenderedVariableDefinition(
        id = "",
        patchId = 1,
        name = "Landbakgrunn",
        shortName = "landbak",
        definition = "For personer født",
        classificationUri = "https://www.ssb.no/en/klass/klassifikasjoner/91",
        unitTypes = emptyList(),
        subjectFields = emptyList(),
        containsSensitivePersonalInformation = false,
        variableStatus = VariableStatus.DRAFT,
        measurementType = null,
        validFrom = LocalDate.of(1960, 1, 1),
        validUntil = LocalDate.of(2021, 1, 1),
        externalReferenceUri = URI("https://example.com/").toURL(),
        relatedVariableDefinitionUris = listOf(),
        owner =
            Owner("", ""),
        contact =
            RenderedContact("", "me@example.com"),
        createdAt = LocalDateTime.parse("2024-06-11T08:15:19"),
        createdBy =
            Person("", ""),
        lastUpdatedAt = LocalDateTime.parse("2024-06-11T08:15:19"),
        lastUpdatedBy =
            Person("", ""),
    )

val JSON_TEST_INPUT =
    """
    {
        "name": {
            "en": "Country Background",
            "nb": "Landbakgrunn",
            "nn": "Landbakgrunn"
        },
        "short_name": "landbak",
        "definition": {
            "en": "C.",
            "nb": "F"
        },
        "classification_reference": "91",
        "unit_types": [
            "03",
            "04",
            "05"
        ],
        "subject_fields": [
            "he04"
        ],
        "contains_sensitive_personal_information": true,
        "measurement_type": "02.01",
        "valid_from": "2024-06-05",
        "valid_until": "2024-06-05",
        "external_reference_uri": "https://example.com/",
        "related_variable_definition_uris": [
            "https://example.com/"
        ],
        "contact": {
            "title": {
                "en": "string",
                "nb": "string",
                "nn": "string"
            },
            "email": "user@example.com"
        }
    }
    """.trimIndent()
