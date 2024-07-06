package no.ssb.metadata.vardef.integrations.vardok

import java.net.URI
import java.net.URL
import no.ssb.metadata.vardef.models.LanguageStringType
import no.ssb.metadata.vardef.models.Owner
import no.ssb.metadata.vardef.models.VariableStatus


fun mapVardokContactDivisionToOwner(vardokItem: VardokResponse): Owner {
    val owner = vardokItem.common?.contactDivision
    val mappedOwner = Owner(owner!!.codeValue, owner.codeText)
    return mappedOwner
}

private fun sliceValidDate(
    range: IntRange,
    validDate: String?,
): String {
    val dateString = validDate!!.slice(range)
    return dateString
}

fun mapValidDateFrom(vardokItem: VardokResponse): CharSequence? {
    val range = 0..9
    val validDate = vardokItem.dc?.valid
    if (validDate != null) {
        if (validDate.isNotEmpty()) {
            return sliceValidDate(range, validDate)
        }
    }
    return null
}

fun mapValidDateUntil(vardokItem: VardokResponse): CharSequence? {
    val range = 13..22
    val validDate = vardokItem.dc?.valid
    if (validDate != null) {
        if (validDate.length >= 20) {
            return sliceValidDate(range, validDate)
        }
    }
    return null
}

fun mapVardokIdentifier(vardokItem: VardokResponse): String {
    val vardokId = vardokItem.id
    val splitId = vardokId.split(":")
    return splitId[splitId.size - 1]
}

data class VarDokStructure(
    val name: LanguageStringType?,
    val shortName: String?,
    val definition: LanguageStringType?,
    var validFrom: String?,
    val validUntil: String?,
    val unitTypes: List<String?>,
    val externalReferenceUri: URL,
    val containsSensitivePersonalInformation: Boolean,
    val subjectFields: List<String?>,
    val classificationReference: String?,
    val contact: String?,
    val measurementType: String?,
    val relatedVariableDefinitionUris: List<String?>
)


fun toVarDefFromVarDok(vardokItem: MutableMap<String, VardokResponse>): VarDokStructure {
    val vardokItemNb = vardokItem["nb"]!!
    val vardokId = mapVardokIdentifier(vardokItemNb)

    return VarDokStructure(
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
        validFrom = mapValidDateFrom(vardokItemNb).toString(),
        validUntil = null, //mapValidDateUntil(vardokItem),
        unitTypes = listOf(unitTypeConverter[vardokItemNb.variable.statisticalUnit]!!),
        externalReferenceUri = URI("https://www.ssb.no/a/xml/metadata/conceptvariable/vardok/$vardokId").toURL(),
        classificationReference = null,
        containsSensitivePersonalInformation = false,
        contact = null,
        measurementType = null,
        relatedVariableDefinitionUris = emptyList(),
        subjectFields = emptyList(),
    )
}
