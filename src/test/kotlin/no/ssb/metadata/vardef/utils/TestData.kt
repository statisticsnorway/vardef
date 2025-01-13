package no.ssb.metadata.vardef.utils

import no.ssb.metadata.vardef.constants.ILLEGAL_SHORNAME_KEYWORD
import no.ssb.metadata.vardef.models.*
import no.ssb.metadata.vardef.services.VariableDefinitionService
import org.bson.types.ObjectId
import org.json.JSONObject
import java.net.URI
import java.time.LocalDate
import java.time.LocalDateTime

val DRAFT_BUS_EXAMPLE =
    Draft(
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
        containsSpecialCategoriesOfPersonalData = false,
        measurementType = "",
        validFrom = LocalDate.of(2021, 1, 1),
        validUntil = null,
        externalReferenceUri = URI("https://www.example.com").toURL(),
        comment = null,
        relatedVariableDefinitionUris = listOf(URI("https://www.example.com").toURL()),
        contact =
            Contact(
                LanguageStringType("", "", ""),
                "",
            ),
    ).toSavedVariableDefinition(TEST_DEVELOPERS_GROUP, TEST_USER)


val DRAFT_EXAMPLE_WITH_VALID_UNTIL =
    Draft(
        name =
            LanguageStringType(
                nb = "Tog",
                nn = "Tog",
                en = "Train",
            ),
        shortName = "train",
        definition =
            LanguageStringType(
                nb = "Et tog er et transportmiddel på skinner.",
                nn = null,
                en = "A train is",
            ),
        classificationReference = "91",
        unitTypes = listOf("", ""),
        subjectFields = listOf("", ""),
        containsSpecialCategoriesOfPersonalData = false,
        measurementType = "",
        validFrom = LocalDate.of(2021, 1, 1),
        validUntil = LocalDate.of(2030, 9, 15),
        externalReferenceUri = URI("https://www.example.com").toURL(),
        comment = null,
        relatedVariableDefinitionUris = listOf(URI("https://www.example.com").toURL()),
        contact =
            Contact(
                LanguageStringType("", "", ""),
                "",
            ),
    ).toSavedVariableDefinition(TEST_DEVELOPERS_GROUP, TEST_USER)

val SAVED_INTERNAL_VARIABLE_DEFINITION =
    SavedVariableDefinition(
        id = ObjectId(),
        definitionId = VariableDefinitionService.generateId(),
        patchId = 1,
        name =
            LanguageStringType(
                nb = "Intern",
                nn = "Intern",
                en = "Internal",
            ),
        shortName = "intern-def",
        definition =
            LanguageStringType(
                nb = "En variabeldefinisjon som er publisert for intern bruk",
                nn = "En variabeldefinisjon som er publisert for intern bruk",
                en = "A variable definition published for internal use",
            ),
        classificationReference = "91",
        unitTypes = listOf("", ""),
        subjectFields = listOf("", ""),
        containsSpecialCategoriesOfPersonalData = false,
        variableStatus = VariableStatus.PUBLISHED_INTERNAL,
        measurementType = "",
        validFrom = LocalDate.of(2024, 1, 1),
        validUntil = LocalDate.of(2030, 1, 1),
        externalReferenceUri = URI("https://www.example.com").toURL(),
        comment = null,
        relatedVariableDefinitionUris = listOf(),
        owner = Owner("my-team", listOf("my-team-developers", "other-group", TEST_DEVELOPERS_GROUP)),
        contact =
            Contact(
                LanguageStringType("", "", ""),
                "me@example.com",
            ),
        createdAt = LocalDateTime.parse("2024-06-11T08:15:19"),
        createdBy = "me@example.com",
        lastUpdatedAt = LocalDateTime.parse("2024-06-11T08:15:19"),
        lastUpdatedBy = "me@example.com",
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
        containsSpecialCategoriesOfPersonalData = false,
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

// Validity Period 1, Patch 1
val INCOME_TAX_VP1_P1 =
    SavedVariableDefinition(
        id = ObjectId(),
        definitionId = VariableDefinitionService.generateId(),
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
        classificationReference = "91",
        unitTypes = listOf("01", "02"),
        subjectFields = listOf("he04"),
        containsSpecialCategoriesOfPersonalData = false,
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
            Owner("pers-skatt", listOf("pers-skatt-developers", TEST_DEVELOPERS_GROUP, "neighbourhood-dogs")),
        contact =
            Contact(
                LanguageStringType("", "", ""),
                "me@example.com",
            ),
        createdAt = LocalDateTime.parse("2024-06-11T08:15:19"),
        createdBy = "me@example.com",
        lastUpdatedAt = LocalDateTime.parse("2024-06-11T08:15:19"),
        lastUpdatedBy = "me@example.com",
    )

// Validity Period 1, Patch 2
val INCOME_TAX_VP1_P2 =
    INCOME_TAX_VP1_P1.copy(
        unitTypes = listOf("01", "02", "03"),
        patchId = 2,
    )

// Validity Period 1, Patch 3
val INCOME_TAX_VP1_P3 =
    INCOME_TAX_VP1_P2.copy(
        patchId = 3,
        unitTypes = listOf("01", "02", "03", "04"),
        comment =
            LanguageStringType(
                "Ny standard for navn til enhetstypeidentifikatorer.",
                null,
                null,
            ),
    )

// Validity Period 1, Patch 4
val INCOME_TAX_VP1_P4 =
    INCOME_TAX_VP1_P3.copy(
        patchId = 4,
        // End Validity Period
        validUntil = LocalDate.of(2020, 12, 31),
    )

// Validity Period 1, Patch 7
// New Patches can be created for 'old' Validity Periods
val INCOME_TAX_VP1_P7 =
    INCOME_TAX_VP1_P4.copy(
        patchId = 7,
        unitTypes = listOf("03", "04"),
    )

// New Validity Period
// Validity Period 2, Patch 5
val INCOME_TAX_VP2_P5 =
    INCOME_TAX_VP1_P4.copy(
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

// Validity Period 2, Patch 6
val INCOME_TAX_VP2_P6 =
    INCOME_TAX_VP2_P5.copy(
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
        INCOME_TAX_VP1_P1,
        INCOME_TAX_VP1_P2,
        INCOME_TAX_VP1_P3,
        INCOME_TAX_VP1_P4,
        INCOME_TAX_VP2_P5,
        INCOME_TAX_VP2_P6,
        INCOME_TAX_VP1_P7,
    ).sortedBy {
        it.patchId
    }

val SAVED_DRAFT_DEADWEIGHT_EXAMPLE =
    SavedVariableDefinition(
        id = ObjectId(),
        definitionId = VariableDefinitionService.generateId(),
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
        classificationReference = "91",
        unitTypes = listOf("01", "02"),
        subjectFields = listOf("he04"),
        containsSpecialCategoriesOfPersonalData = false,
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
            Owner("skip-stat", listOf("skip-stat-developers", TEST_DEVELOPERS_GROUP)),
        contact =
            Contact(
                LanguageStringType("", "", ""),
                "me@example.com",
            ),
        createdAt = LocalDateTime.parse("2024-06-11T08:15:19"),
        createdBy = "me@example.com",
        lastUpdatedAt = LocalDateTime.parse("2024-06-11T08:15:19"),
        lastUpdatedBy = "me@example.com",
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
        containsSpecialCategoriesOfPersonalData = false,
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
        containsSpecialCategoriesOfPersonalData = false,
        measurementType = null,
        validFrom = LocalDate.of(2021, 1, 1),
        validUntil = LocalDate.of(2021, 1, 1),
        externalReferenceUri = URI("https://example.com/").toURL(),
        comment = null,
        relatedVariableDefinitionUris = listOf(),
        contact = null,
        lastUpdatedAt = LocalDateTime.parse("2024-06-11T08:15:19"),
    )

val SAVED_BYDEL_WITH_ILLEGAL_SHORTNAME =
    SavedVariableDefinition(
        id = ObjectId(),
        definitionId = VariableDefinitionService.generateId(),
        patchId = 1,
        name =
            LanguageStringType(
                nb = "Bydel",
                nn = null,
                en = null,
            ),
        shortName = ILLEGAL_SHORNAME_KEYWORD + "abcd",
        definition =
            LanguageStringType(
                nb = "Bydel",
                nn = null,
                en = null,
            ),
        classificationReference = "91",
        unitTypes = listOf("01", "02"),
        subjectFields = listOf("he04"),
        containsSpecialCategoriesOfPersonalData = false,
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
            Owner("skip-stat", listOf("skip-stat-developers", TEST_DEVELOPERS_GROUP)),
        contact =
            Contact(
                LanguageStringType("", "", ""),
                "me@example.com",
            ),
        createdAt = LocalDateTime.parse("2024-06-11T08:15:19"),
        createdBy = "me@example.com",
        lastUpdatedAt = LocalDateTime.parse("2024-06-11T08:15:19"),
        lastUpdatedBy = "me@example.com",
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
        containsSpecialCategoriesOfPersonalData = false,
        variableStatus = VariableStatus.DRAFT,
        measurementType = null,
        validFrom = LocalDate.of(1960, 1, 1),
        validUntil = LocalDate.of(2021, 1, 1),
        externalReferenceUri = URI("https://example.com/").toURL(),
        comment = null,
        relatedVariableDefinitionUris = listOf(),
        owner =
            Owner(
                team = TEST_TEAM,
                groups = listOf(TEST_DEVELOPERS_GROUP),
            ),
        contact = null,
        createdAt = LocalDateTime.now(),
        createdBy = TEST_USER,
        lastUpdatedAt = LocalDateTime.now(),
        lastUpdatedBy = TEST_USER,
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
        "contains_special_categories_of_personal_data": true,
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

val UPDATE_DRAFT_CLASSIFICATION_REFERENCE =
    UpdateDraft(
        name = null,
        shortName = null,
        definition = null,
        classificationReference = "92",
        unitTypes = null,
        subjectFields = null,
        containsSpecialCategoriesOfPersonalData = null,
        measurementType = null,
        validFrom = null,
        externalReferenceUri = null,
        relatedVariableDefinitionUris = null,
        contact = null,
        comment = null,
        owner = null,
        variableStatus = null,
    )
