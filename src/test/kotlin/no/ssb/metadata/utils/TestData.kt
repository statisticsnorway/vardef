
import io.viascom.nanoid.NanoId
import no.ssb.metadata.models.*
import org.bson.types.ObjectId
import java.net.URI
import java.time.LocalDate
import java.time.LocalDateTime

val INPUT_VARIABLE_DEFINITION =
    InputVariableDefinition(
        id = null,
        name = LanguageStringType(nb = "Landbakgrunn", nn = "Landbakgrunn", en = "Country Background"),
        shortName = "landbak",
        definition = LanguageStringType(nb = "For personer født", nn = null, en = "Country background is"),
        classificationReference = "91",
        unitTypes = listOf("", ""),
        subjectFields = listOf("", ""),
        containsUnitIdentifyingInformation = false,
        containsSensitivePersonalInformation = false,
        variableStatus = VariableStatus.DRAFT,
        measurementType = "",
        validFrom = LocalDate.of(2021, 1, 4),
        validUntil = LocalDate.of(2021, 1, 4),
        externalReferenceUri = URI("https://www.example.com").toURL(),
        relatedVariableDefinitionUris = listOf(URI("https://www.example.com").toURL()),
        contact = Contact(LanguageStringType("", "", ""), ""),
    )

val INPUT_VARIABLE_DEFINITION_COPY =
    INPUT_VARIABLE_DEFINITION.copy(
        name = LanguageStringType(nb = "Landbakgrunn 2", nn = "Landbakgrunn 2", en = "Country Background 2"),
        shortName = "landbak 2",
    )

val INPUT_VARIABLE_DEFINITION_NO_NAME =
    INPUT_VARIABLE_DEFINITION.copy(
        name = LanguageStringType(nb = "Landbakgrunn", nn = "", en = null),
        shortName = "landbak 2",
    )

val SAVED_VARIABLE_DEFINITION =
    SavedVariableDefinition(
        id = ObjectId(),
        definitionId = NanoId.generate(8),
        name = LanguageStringType(nb = "Landbakgrunn", nn = "Landbakgrunn", en = "Country Background"),
        shortName = "landbak",
        definition = LanguageStringType(nb = "For personer født", nn = "For personer født", en = "Country background is"),
        classificationUri = "https://www.ssb.no/en/klass/klassifikasjoner/91",
        unitTypes = listOf("01", "02"),
        subjectFields = listOf("he04"),
        containsUnitIdentifyingInformation = false,
        containsSensitivePersonalInformation = false,
        variableStatus = VariableStatus.DRAFT,
        measurementType = "02.01",
        validFrom = LocalDate.of(2021, 1, 4),
        validUntil = LocalDate.of(2021, 1, 4),
        externalReferenceUri = URI("https://example.com/").toURL(),
        relatedVariableDefinitionUris = listOf(),
        owner = Owner("", ""),
        contact = Contact(LanguageStringType("", "", ""), "me@example.com"),
        createdAt = LocalDateTime.parse("2024-06-11T08:15:19"),
        createdBy = Person("", ""),
        lastUpdatedAt = LocalDateTime.parse("2024-06-11T08:15:19"),
        lastUpdatedBy = Person("", ""),
    )

val SAVED_VARIABLE_DEFINITION_COPY =
    SAVED_VARIABLE_DEFINITION.copy(
        id = ObjectId(),
        definitionId = NanoId.generate(8),
        name = LanguageStringType(nb = "Landbakgrunn 2", nn = "Landbakgrunn 2", en = "Country Background 2"),
    )

val RENDERED_VARIABLE_DEFINITION =
    RenderedVariableDefinition(
        id = "",
        name = "Landbakgrunn",
        shortName = "landbak",
        definition = "For personer født",
        classificationUri = "https://www.ssb.no/en/klass/klassifikasjoner/91",
        unitTypes = emptyList(),
        subjectFields = emptyList(),
        containsUnitIdentifyingInformation = false,
        containsSensitivePersonalInformation = false,
        variableStatus = VariableStatus.DRAFT,
        measurementType = null,
        validFrom = LocalDate.of(2021, 1, 4),
        validUntil = LocalDate.of(2021, 1, 4),
        externalReferenceUri = URI("https://example.com/").toURL(),
        relatedVariableDefinitionUris = listOf(),
        owner = Owner("", ""),
        contact = RenderedContact("", "me@example.com"),
        createdAt = LocalDateTime.parse("2024-06-11T08:15:19"),
        createdBy = Person("", ""),
        lastUpdatedAt = LocalDateTime.parse("2024-06-11T08:15:19"),
        lastUpdatedBy = Person("", ""),
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
        "contains_unit_identifying_information": true,
        "contains_sensitive_personal_information": true,
        "variable_status": "DRAFT",
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
