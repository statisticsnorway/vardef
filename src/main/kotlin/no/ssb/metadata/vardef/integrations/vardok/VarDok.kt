package no.ssb.metadata.vardef.integrations.vardok

import no.ssb.metadata.vardef.models.InputVariableDefinition
import no.ssb.metadata.vardef.models.LanguageStringType
import no.ssb.metadata.vardef.models.Owner
import no.ssb.metadata.vardef.models.VariableStatus
import java.net.URI
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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

private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

fun mapValidDateFrom(vardokItem: VardokResponse): LocalDate {
    val range = 0..9
    val validDate = vardokItem.dc?.valid
    if (!validDate.isNullOrEmpty()) {
        return LocalDate.parse(sliceValidDate(range, validDate), formatter)
    }
    throw MissingValidDatesException()
}

fun mapValidDateUntil(vardokItem: VardokResponse): LocalDate? {
    val range = 13..22
    val validDate = vardokItem.dc?.valid
    if (validDate != null && validDate.length >= 20) {
        return LocalDate.parse(sliceValidDate(range, validDate), formatter)
    }
    return null
}

fun mapVardokIdentifier(vardokItem: VardokResponse): String {
    val vardokId = vardokItem.id
    val splitId = vardokId.split(":")
    return splitId[splitId.size - 1]
}

fun toVarDefFromVarDok(vardokItem: MutableMap<String, VardokResponse>): InputVariableDefinition {
    val vardokItemNb = vardokItem["nb"]!!
    val vardokId = mapVardokIdentifier(vardokItemNb)

    val vardefInput =
        mapValidDateFrom(vardokItemNb).let {
            InputVariableDefinition(
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
                validFrom = mapValidDateFrom(vardokItemNb),
                validUntil = mapValidDateUntil(vardokItemNb),
                unitTypes = listOf(unitTypeConverter[vardokItemNb.variable.statisticalUnit]!!),
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
        }
    return vardefInput
}
