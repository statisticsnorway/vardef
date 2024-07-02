package no.ssb.metadata.vardef.integrations.vardok

import no.ssb.metadata.models.LanguageStringType
import org.slf4j.LoggerFactory
import java.time.LocalDate

data class RenderVarDok(val name: LanguageStringType, val shortName: String?, val definition: LanguageStringType, val validFrom: LocalDate)

fun migrateVarDok(vardokItem: FIMD): RenderVarDok {
    val name = LanguageStringType(vardokItem.common?.title, null, null)
    val definition = LanguageStringType(vardokItem.common?.description, null, null)
    val shortName = vardokItem.variable?.shortNameWeb?.codeText
    val validFrom = LocalDate.parse(vardokItem.lastChangedDate)
    val renderVarDok = RenderVarDok(name, shortName, definition, validFrom)
    return renderVarDok
}

class VarDokValidDates(vardokItem: FIMD) {
    private val validDate = vardokItem.dc?.valid
    private val logger = LoggerFactory.getLogger(VarDokValidDates::class.java)

    private fun mapDateToLocalDate(range: IntRange): String {
        val dateString = validDate!!.slice(range)
        logger.info("Valid date: $dateString")
        return dateString
    }
    fun mapValidDateFrom(): String?{
        val range = 0..9
        if (validDate != null) {
            if(validDate.isNotEmpty()){
                return mapDateToLocalDate(range)
            }
        }
        return null
    }
    fun mapValidDateUntil(): String?{
        val range =13..22
        if (validDate != null) {
            if(validDate.length >= 20){
                return mapDateToLocalDate(range)
            }
        }
        return null
    }
}