package no.ssb.metadata.vardef.integrations.vardok

import no.ssb.metadata.models.LanguageStringType
import org.slf4j.LoggerFactory

data class RenderVarDok(
    val name: LanguageStringType,
    val shortName: String?,
    val definition: LanguageStringType,
    val validFrom: String,
    val validUntil: String?,
)

fun migrateVarDok(vardokItem: FIMD): RenderVarDok? {
    val name = LanguageStringType(vardokItem.common?.title, null, null)
    val shortName = vardokItem.variable?.shortNameWeb?.codeText
    val definition = LanguageStringType(vardokItem.common?.description, null, null)
    val validFrom = mapValidDateFrom(vardokItem)
    val validUntil = mapValidDateUntil(vardokItem)
    return if (validFrom == null) {
        null
    } else {
        RenderVarDok(name, shortName, definition, validFrom, validUntil)
    }
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
