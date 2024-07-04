package no.ssb.metadata.vardef.integrations.vardok

import no.ssb.metadata.vardef.models.InputVariableDefinition
import no.ssb.metadata.vardef.models.LanguageStringType
import no.ssb.metadata.vardef.models.Owner
import no.ssb.metadata.vardef.models.VariableStatus
import java.net.URI
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun mapVardokContactDivisionToOwner(vardokItem: FIMD): Owner {
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

fun mapValidDateFrom(vardokItem: FIMD): CharSequence? {
    val range = 0..9
    val validDate = vardokItem.dc?.valid
    if (validDate != null) {
        if (validDate.isNotEmpty()) {
            return sliceValidDate(range, validDate)
        }
    }
    return null
}

fun mapValidDateUntil(vardokItem: FIMD): CharSequence? {
    val range = 13..22
    val validDate = vardokItem.dc?.valid
    if (validDate != null) {
        if (validDate.length >= 20) {
            return sliceValidDate(range, validDate)
        }
    }
    return null
}

fun mapVardokIdentifier(vardokItem: FIMD): String {
    val vardokId = vardokItem.id
    val splitId = vardokId.split(":")
    return splitId[splitId.size - 1]
}

fun toVarDefFromVarDok(vardokItems: MutableMap<String, FIMD>): InputVariableDefinition {
    val vardokItem = vardokItems["nb"]!!
    val vardokId = mapVardokIdentifier(vardokItem)

    val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    val vardefInput =
        InputVariableDefinition(
            name =
                LanguageStringType(
                    vardokItem.common?.title,
                    vardokItems["nn"]?.common?.title,
                    vardokItems["en"]?.common?.title,
                ),
            shortName = vardokItem.variable?.dataElementName!!,
            definition =
                LanguageStringType(
                    vardokItem.common?.description,
                    vardokItems["nn"]?.common?.description,
                    vardokItems["en"]?.common?.description,
                ),
            validFrom = mapValidDateFrom(vardokItem)?.let { LocalDate.parse(it, formatter) }!!,
            validUntil = mapValidDateUntil(vardokItem)?.let { LocalDate.parse(it, formatter) },
            unitTypes = listOf(unitTypeConverter[vardokItem.variable.statisticalUnit]!!),
            externalReferenceUri = URI("https://www.ssb.no/a/xml/metadata/conceptvariable/vardok/$vardokId").toURL(),
            variableStatus = VariableStatus.DRAFT,
            classificationReference = null,
            containsSensitivePersonalInformation = false,
            contact = null,
            id = null,
            measurementType = null,
            relatedVariableDefinitionUris = emptyList(),
            subjectFields = emptyList(),
            // TODO Consider if we want to use owner by patching the variable definition
            // owner = mapVardokContactDivisionToOwner(vardokItem),
        )
    return vardefInput
}
