package no.ssb.metadata.vardef.integrations.vardok

import io.micronaut.serde.annotation.Serdeable
import io.micronaut.serde.config.naming.SnakeCaseStrategy
import no.ssb.metadata.vardef.models.InputVariableDefinition
import no.ssb.metadata.vardef.models.LanguageStringType
import no.ssb.metadata.vardef.models.Owner
import no.ssb.metadata.vardef.models.VariableStatus
import org.slf4j.LoggerFactory
import java.net.URI
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Serdeable(naming = SnakeCaseStrategy::class)
data class RenderVarDok(
    val name: LanguageStringType?,
    val shortName: String?,
    val definition: LanguageStringType?,
    var validFrom: String?,
    val validUntil: String?,
    val unitTypes: List<String?>,
    val externalReferenceUri: String?,
    val variableStatus: String,
    val owner: Owner,
)

/*fun toRenderVarDok(vardokItem: FIMD): RenderVarDok {
    val vardokId = mapVardokIdentifier(vardokItem)
    val renderVarDok =
        RenderVarDok(
            name = LanguageStringType(vardokItem.common?.title, null, null),
            shortName = vardokItem.variable?.dataElementName,
            definition = LanguageStringType(vardokItem.common?.description, null, null),
            validFrom = mapValidDateFrom(vardokItem).toString(),
            validUntil = mapValidDateUntil(vardokItem).toString(),
            unitTypes = listOf(unitTypeConverter[vardokItem.variable?.statisticalUnit]),
            externalReferenceUri = "https://www.ssb.no/a/xml/metadata/conceptvariable/vardok/$vardokId",
            variableStatus = "DRAFT",
            owner = mapVardokContactDivisionToOwner(vardokItem),
        )
    return renderVarDok
}*/

fun mapVardokContactDivisionToOwner(vardokItem: FIMD): Owner {
    val owner = vardokItem.common?.contactDivision
    val mappedOwner = Owner(owner!!.codeValue, owner.codeText)
    return mappedOwner
}

private fun sliceValidDate(
    range: IntRange,
    validDate: String?,
): String {
    val logger = LoggerFactory.getLogger(RenderVarDok::class.java)
    val dateString = validDate!!.slice(range)
    logger.info("Valid date: $dateString")
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

fun toRenderVarDokMultiLang(vardokItems: MutableMap<String, FIMD>): InputVariableDefinition {
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
