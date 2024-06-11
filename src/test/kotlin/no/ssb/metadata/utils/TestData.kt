import com.fasterxml.jackson.annotation.JsonIgnore
import io.micronaut.data.annotation.GeneratedValue
import io.micronaut.data.annotation.Id
import io.micronaut.json.tree.JsonObject
import io.micronaut.serde.annotation.Serdeable
import no.ssb.metadata.models.*
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap
import io.micronaut.serde.config.naming.SnakeCaseStrategy
import io.viascom.nanoid.NanoId
import org.bson.types.ObjectId
import org.json.JSONObject

val INPUT_VARIABLE_DEFINITION = InputVariableDefinition(
    id = null,
    name = LanguageStringType(nb = "Landbakgrunn", nn = "Landbakgrunn", en = "Country Background"),
    shortName = "landbak",
    definition =  LanguageStringType(nb = "For personer født", nn = null, en = "Country background is"),
    classificationReference = "91",
    unitTypes = listOf("",""),
    subjectFields = listOf("", ""),
    containsUnitIdentifyingInformation =  false,
    containsSensitivePersonalInformation = false,
    variableStatus = "Draft",
    measurementType = "",
    validFrom = "",
    validUntil = "",
    externalReferenceUri = "https://www.example.com",
    relatedVariableDefinitionUris = listOf("https://www.example.com"),
    contact = Contact(LanguageStringType("", "", ""), ""),
)

val INPUT_VARIABLE_DEFINITION_COPY = INPUT_VARIABLE_DEFINITION.copy(
    name = LanguageStringType(nb = "Landbakgrunn 2", nn = "Landbakgrunn 2", en = "Country Background 2"),
    shortName = "landbak 2",
)

val SAVED_VARIABLE_DEFINITION = SavedVariableDefinition(
    definitionId = "",
    name = LanguageStringType(nb = "Landbakgrunn", nn = "Landbakgrunn", en = "Country Background"),
    shortName = "landbak",
    definition = LanguageStringType(nb = "For personer født", nn = "For personer født", en = "Country background is"),
    classificationUri = "https://www.ssb.no/en/klass/klassifikasjoner/91",
    unitTypes = listOf(KlassReference("https://example.com/", "", "")),
    subjectFields = listOf(KlassReference("https://example.com/", "", "")),
    containsUnitIdentifyingInformation = false,
    containsSensitivePersonalInformation = false,
    variableStatus = "",
    measurementType = KlassReference("https://example.com/", "", ""),
    validFrom = "2024-06-11",
    validUntil = "2024-06-11",
    externalReferenceUri =  "https://example.com/",
    relatedVariableDefinitionUris = listOf("https://example.com/"),
    owner = Owner("", ""),
    contact = Contact(LanguageStringType("", "", ""), ""),
    createdAt = "2024-06-11T08:15:19.421Z",
    createdBy = Person("", ""),
    lastUpdatedAt = "2024-06-11T08:15:19.421Z",
    lastUpdatedBy = Person("", "")
)


val JSON_TEST_INPUT =
    """
    {
        "name": {
            "en": "Country Background",
            "nb": "Landbakgrunn",
            "nn": "Landbakgrunn"
        },
        "short_name": "landbak 2",
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
