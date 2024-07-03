package no.ssb.metadata.vardef.integrations.vardok

import no.ssb.metadata.models.LanguageStringType
import org.slf4j.LoggerFactory

data class RenderVarDok(
    val name: LanguageStringType,
    val shortName: String?,
    val definition: LanguageStringType,
    val validFrom: String,
    val validUntil: String?,
    val unitTypes: List<String?>,
)

fun toRenderVarDok(vardokItem: FIMD): RenderVarDok? {
    val renderVarDok =
        mapValidDateFrom(vardokItem)?.let {
            RenderVarDok(
                name = LanguageStringType(vardokItem.common?.title, null, null),
                // shortName = vardokItem.variable?.shortNameWeb?.codeValue,
                shortName = vardokItem.variable?.dataElementName,
                definition = LanguageStringType(vardokItem.common?.description, null, null),
                validFrom = it,
                validUntil = mapValidDateUntil(vardokItem),
                unitTypes = listOf(unitTypeConverter[vardokItem.variable?.statisticalUnit])
            )
        }
    return renderVarDok
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

fun mapValidDateFrom(vardokItem: FIMD): String? {
    val range = 0..9
    val validDate = vardokItem.dc?.valid
    if (validDate != null) {
        if (validDate.isNotEmpty()) {
            return sliceValidDate(range, validDate)
        }
    }
    return null
}

fun mapValidDateUntil(vardokItem: FIMD): String? {
    val range = 13..22
    val validDate = vardokItem.dc?.valid
    if (validDate != null) {
        if (validDate.length >= 20) {
            return sliceValidDate(range, validDate)
        }
    }
    return null
}

fun toRenderVarDokMultiLang(vardokItems: MutableMap<String, FIMD?>): RenderVarDok? {
    if (vardokItems["nb"] == null) {
        return null
    }
    val vardokItem = vardokItems["nb"]!!
    val renderVarDok =
        mapValidDateFrom(vardokItem)?.let {
            RenderVarDok(
                name = LanguageStringType(vardokItem.common?.title,  vardokItems["nn"]?.common?.title, vardokItems["en"]?.common?.title),
                shortName = vardokItem.variable?.dataElementName,
                definition = LanguageStringType(vardokItem.common?.description, vardokItems["nn"]?.common?.description, vardokItems["en"]?.common?.description),
                validFrom = it,
                validUntil = mapValidDateUntil(vardokItem),
                unitTypes = listOf(unitTypeConverter[vardokItem.variable?.statisticalUnit])
            )
        }
    return renderVarDok
}

