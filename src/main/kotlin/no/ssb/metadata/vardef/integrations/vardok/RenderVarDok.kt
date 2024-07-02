package no.ssb.metadata.vardef.integrations.vardok

import no.ssb.metadata.models.LanguageStringType
import java.time.LocalDate

data class RenderVarDok(val name: LanguageStringType, val shortName: String?,val definition: LanguageStringType, val validFrom: LocalDate)

fun toRenderVarDok(vardokItem: FIMD): RenderVarDok {
    val name = LanguageStringType(vardokItem.common?.title,null,null)
    val definition = LanguageStringType(vardokItem.common?.description,null,null)
    val shortName = vardokItem.variable?.shortNameWeb?.codeText
    val validFrom = LocalDate.parse(vardokItem.lastChangedDate)
    val renderVarDok = RenderVarDok(name, shortName, definition, validFrom)
    return renderVarDok
}
