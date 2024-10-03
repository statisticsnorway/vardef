package no.ssb.metadata.vardef.integrations.vardok

import no.ssb.metadata.vardef.models.LanguageStringType

data class VarDok(
    val name: LanguageStringType?,
    val shortName: String?,
    val definition: LanguageStringType?,
    var validFrom: String?,
    val unitTypes: List<String?>,
    val externalReferenceUri: String,
    val containsSensitivePersonalInformation: Boolean,
    val subjectFields: List<String?>,
    val classificationReference: String?,
    val contact: String?,
    val measurementType: String?,
    val relatedVariableDefinitionUris: List<String?>,
)

fun toVarDefFromVarDok(vardokItem: MutableMap<String, VardokResponse>): VarDok {
    val vardokItemNb = vardokItem["nb"]!!
    val vardokId = mapVardokIdentifier(vardokItemNb)

    return VarDok(
        name =
            LanguageStringType(
                vardokItemNb.common?.title,
                vardokItem["nn"]?.common?.title,
                vardokItem["en"]?.common?.title,
            ),
        shortName = vardokItemNb.variable?.dataElementName!!,
        definition =
            LanguageStringType(
                vardokItemNb.common?.description,
                vardokItem["nn"]?.common?.description,
                vardokItem["en"]?.common?.description,
            ),
        validFrom = getValidDates(vardokItemNb).first,
        unitTypes = listOf(unitTypeConverter[vardokItemNb.variable.statisticalUnit]!!),
        externalReferenceUri = "https://www.ssb.no/a/xml/metadata/conceptvariable/vardok/$vardokId",
        containsSensitivePersonalInformation = false,
        subjectFields = emptyList(),
        classificationReference = null,
        contact = null,
        measurementType = null,
        relatedVariableDefinitionUris = emptyList(),
    )
}
