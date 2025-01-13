package no.ssb.metadata.vardef.integrations.vardok.models

import no.ssb.metadata.vardef.models.LanguageStringType

data class VardefInput(
    val name: LanguageStringType?,
    val shortName: String?,
    val definition: LanguageStringType?,
    var validFrom: String,
    val validUntil: String?,
    val unitTypes: List<String?>,
    val externalReferenceUri: String,
    val comment: LanguageStringType?,
    val containsSpecialCategoriesOfPersonalData: Boolean,
    val subjectFields: List<String?>,
    val classificationReference: String?,
    val contact: String?,
    val measurementType: String?,
    val relatedVariableDefinitionUris: List<String?>,
)
