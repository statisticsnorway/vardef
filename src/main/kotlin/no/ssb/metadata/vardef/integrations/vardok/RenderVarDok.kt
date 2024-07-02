package no.ssb.metadata.vardef.integrations.vardok

import no.ssb.metadata.models.LanguageStringType
import java.time.LocalDate

data class RenderVarDok(
    val name: LanguageStringType,
    val shortName: String?,
    val definition: LanguageStringType,
    val validFrom: String,
    val unitTypes: List<String?>
)

fun toRenderVarDok(vardokItem: FIMD): RenderVarDok {
    val renderVarDok = RenderVarDok(
        name = LanguageStringType(vardokItem.common?.title, null, null),
        shortName = vardokItem.variable?.shortNameWeb?.codeValue,
        definition = LanguageStringType(vardokItem.common?.description, null, null),
        validFrom = vardokItem.lastChangedDate,
        unitTypes =  listOf(unitTypeConverter[vardokItem.variable?.statisticalUnit]))
    return renderVarDok
}




