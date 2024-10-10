package no.ssb.metadata.vardef.utils

import io.viascom.nanoid.NanoId
import no.ssb.metadata.vardef.models.*
import org.bson.types.ObjectId
import org.json.JSONObject
import java.net.URI
import java.time.LocalDate
import java.time.LocalDateTime

val DRAFT_BUS_EXAMPLE =
    Draft(
        id = NanoId.generate(8),
        name =
            LanguageStringType(
                nb = "Buss",
                nn = "Buss",
                en = "Bus",
            ),
        shortName = "bus",
        definition =
            LanguageStringType(
                nb = "En buss er en bil for persontransport med over 8 sitteplasser i tillegg til førersetet.",
                nn = null,
                en = "A bus is",
            ),
        classificationReference = "91",
        unitTypes = listOf("", ""),
        subjectFields = listOf("", ""),
        containsSensitivePersonalInformation = false,
        variableStatus = VariableStatus.DRAFT,
        measurementType = "",
        validFrom = LocalDate.of(2021, 1, 1),
        externalReferenceUri = URI("https://www.example.com").toURL(),
        comment = null,
        relatedVariableDefinitionUris = listOf(URI("https://www.example.com").toURL()),
        contact =
            Contact(
                LanguageStringType("", "", ""),
                "",
            ),
    )

val SAVED_DEPRECATED_VARIABLE_DEFINITION =
    SavedVariableDefinition(
        id = ObjectId(),
        definitionId = NanoId.generate(8),
        patchId = 5,
        name =
            LanguageStringType(
                nb = "A",
                nn = "A",
                en = "B",
            ),
        shortName = "alphabet",
        definition =
            LanguageStringType(
                nb = "Test bokstaver.",
                nn = null,
                en = "Letters",
            ),
        classificationUri = "91",
        unitTypes = listOf("", ""),
        subjectFields = listOf("", ""),
        containsSensitivePersonalInformation = false,
        variableStatus = VariableStatus.DEPRECATED,
        measurementType = "",
        validFrom = LocalDate.of(2021, 1, 1),
        validUntil = null,
        externalReferenceUri = URI("https://www.example.com").toURL(),
        comment = null,
        relatedVariableDefinitionUris = listOf(),
        owner = Owner("my-team", listOf("my-team-developers", "other-group")),
        contact =
            Contact(
                LanguageStringType("", "", ""),
                "me@example.com",
            ),
        createdAt = LocalDateTime.parse("2024-06-11T08:15:19"),
        createdBy = Person("", ""),
        lastUpdatedAt = LocalDateTime.parse("2024-06-11T08:15:19"),
        lastUpdatedBy = Person("", ""),
    )

val VALIDITY_PERIOD_TAX_EXAMPLE =
    ValidityPeriod(
        name =
            LanguageStringType(
                nb = "Inntektsskatt",
                nn = "Inntektsskatt",
                en = "Income tax",
            ),
        definition =
            LanguageStringType(
                nb = "Inntektsskatt utlignes til staten på grunnlag av alminnelig inntekt.",
                nn = null,
                en = "Income tax",
            ),
        classificationReference = "91",
        unitTypes = listOf("", ""),
        subjectFields = listOf("", ""),
        containsSensitivePersonalInformation = false,
        variableStatus = VariableStatus.PUBLISHED_INTERNAL,
        measurementType = "",
        validFrom = LocalDate.of(2021, 1, 1),
        externalReferenceUri = URI("https://www.example.com").toURL(),
        comment = null,
        relatedVariableDefinitionUris = listOf(URI("https://www.example.com").toURL()),
        contact =
            Contact(
                LanguageStringType("", "", ""),
                "",
            ),
    )

val INCOME_TAX_PATCH_1 =
    SavedVariableDefinition(
        id = ObjectId(),
        definitionId = NanoId.generate(8),
        patchId = 1,
        name =
            LanguageStringType(
                nb = "Inntektsskatt",
                nn = "Inntektsskatt",
                en = "Income tax",
            ),
        shortName = "intskatt",
        definition =
            LanguageStringType(
                nb = "Inntektsskatt utlignes til staten på grunnlag av alminnelig inntekt.",
                nn = "Inntektsskatt utlignes til staten på grunnlag av alminnelig inntekt.",
                en = "Income tax",
            ),
        classificationUri = "91",
        unitTypes = listOf("01", "02"),
        subjectFields = listOf("he04"),
        containsSensitivePersonalInformation = false,
        variableStatus = VariableStatus.PUBLISHED_EXTERNAL,
        measurementType = "02.01",
        validFrom = LocalDate.of(1980, 1, 1),
        validUntil = null,
        externalReferenceUri = URI("https://example.com/").toURL(),
        comment =
            LanguageStringType(
                "Variabelen er viktig",
                null,
                null,
            ),
        relatedVariableDefinitionUris = listOf(),
        owner =
            Owner("pers-skatt", listOf("pers-skatt-developers", "neighbourhood-dogs")),
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

val INCOME_TAX_PATCH_2 =
    INCOME_TAX_PATCH_1.copy(
        unitTypes = listOf("01", "02", "03"),
        patchId = 2,
    )

val INCOME_TAX_PATCH_3 =
    INCOME_TAX_PATCH_2.copy(
        patchId = 3,
        unitTypes = listOf("01", "02", "03", "04"),
        comment =
            LanguageStringType(
                "Ny standard for navn til enhetstypeidentifikatorer.",
                null,
                null,
            ),
    )

val INCOME_TAX_PATCH_4 =
    INCOME_TAX_PATCH_3.copy(
        patchId = 4,
        // End Validity Period
        validUntil = LocalDate.of(2020, 12, 31),
    )

// New Patches can be created for 'old' Validity Periods
val INCOME_TAX_PATCH_7 =
    INCOME_TAX_PATCH_4.copy(
        patchId = 7,
        unitTypes = listOf("03", "04"),
    )

// New Validity Period

val INCOME_TAX_PATCH_5 =
    INCOME_TAX_PATCH_4.copy(
        patchId = 5,
        validFrom = LocalDate.of(2021, 1, 1),
        validUntil = null,
        definition =
            LanguageStringType(
                "Intektsskatt ny definisjon",
                "Intektsskatt ny definisjon",
                "Income tax new definition",
            ),
    )

val INCOME_TAX_PATCH_6 =
    INCOME_TAX_PATCH_5.copy(
        patchId = 6,
        unitTypes = listOf("01", "02"),
        comment =
            LanguageStringType(
                "Gjelder for færre enhetstyper",
                null,
                null,
            ),
    )

val ALL_INCOME_TAX_PATCHES =
    listOf(
        INCOME_TAX_PATCH_1,
        INCOME_TAX_PATCH_2,
        INCOME_TAX_PATCH_3,
        INCOME_TAX_PATCH_4,
        INCOME_TAX_PATCH_5,
        INCOME_TAX_PATCH_6,
        INCOME_TAX_PATCH_7,
    ).sortedBy {
        it.patchId
    }

val SAVED_DRAFT_DEADWEIGHT_EXAMPLE =
    SavedVariableDefinition(
        id = ObjectId(),
        definitionId = NanoId.generate(8),
        patchId = 1,
        name =
            LanguageStringType(
                nb = "Dødvekt",
                nn = "Dødvekt",
                en = "Dead weight",
            ),
        shortName = "dvkt",
        definition =
            LanguageStringType(
                nb = "Dødvekt er den største vekt skipet kan bære av last og beholdninger.",
                nn = null,
                en = "Dead weight",
            ),
        classificationUri = "91",
        unitTypes = listOf("01", "02"),
        subjectFields = listOf("he04"),
        containsSensitivePersonalInformation = false,
        variableStatus = VariableStatus.DRAFT,
        measurementType = "02.01",
        validFrom = LocalDate.of(2021, 1, 1),
        validUntil = null,
        externalReferenceUri = URI("https://example.com/").toURL(),
        comment =
            LanguageStringType(
                "Legger til merknad",
                null,
                null,
            ),
        relatedVariableDefinitionUris = listOf(),
        owner =
            Owner("skip-stat", listOf("skip-stat-developers")),
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
        measurementType = null,
        validFrom = LocalDate.of(1960, 1, 1),
        validUntil = LocalDate.of(2021, 1, 1),
        externalReferenceUri = URI("https://example.com/").toURL(),
        comment = null,
        relatedVariableDefinitionUris = listOf(),
        contact =
            RenderedContact("", "me@example.com"),
        lastUpdatedAt = LocalDateTime.parse("2024-06-11T08:15:19"),
    )

val RENDERED_VARIABLE_DEFINITION_NULL_CONTACT =
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
        measurementType = null,
        validFrom = LocalDate.of(2021, 1, 1),
        validUntil = LocalDate.of(2021, 1, 1),
        externalReferenceUri = URI("https://example.com/").toURL(),
        comment = null,
        relatedVariableDefinitionUris = listOf(),
        contact = null,
        lastUpdatedAt = LocalDateTime.parse("2024-06-11T08:15:19"),
    )

val COMPLETE_RESPONSE =
    CompleteResponse(
        id = "",
        patchId = 1,
        name =
            LanguageStringType(
                nb = "For personer født",
                nn = null,
                en = null,
            ),
        shortName = "landbak",
        definition =
            LanguageStringType(
                nb = "For personer født",
                nn = null,
                en = null,
            ),
        classificationReference = "https://www.ssb.no/en/klass/klassifikasjoner/91",
        unitTypes = emptyList(),
        subjectFields = emptyList(),
        containsSensitivePersonalInformation = false,
        variableStatus = VariableStatus.DRAFT,
        measurementType = null,
        validFrom = LocalDate.of(1960, 1, 1),
        validUntil = LocalDate.of(2021, 1, 1),
        externalReferenceUri = URI("https://example.com/").toURL(),
        comment = null,
        relatedVariableDefinitionUris = listOf(),
        owner = null,
        contact = null,
        createdAt = LocalDateTime.now(),
        createdBy = null,
        lastUpdatedAt = LocalDateTime.now(),
        lastUpdatedBy = null,
    )

fun jsonTestInput() =
    JSONObject(
        """
    {
        "name": {
            "en": "Income tax",
            "nb": "Inntektsskatt",
            "nn": "Inntektsskatt"
        },
        "short_name": "my_new_short_name",
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
    """,
    )
