import no.ssb.metadata.models.*

val SIMPLE_VARIABLE_DEFINITION = InputVariableDefinition(
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



val VARIABLE_JSON_INPUT = """
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
                "en": "a",
                "nb": "b",
                "nn": "c"
            },
            "email": "user@example.com"
        }
    }
    """.trimIndent()