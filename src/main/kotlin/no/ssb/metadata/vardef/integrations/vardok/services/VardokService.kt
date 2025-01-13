package no.ssb.metadata.vardef.integrations.vardok.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import io.micronaut.context.annotation.Prototype
import io.micronaut.core.annotation.Introspected
import io.viascom.nanoid.NanoId
import no.ssb.metadata.vardef.constants.ILLEGAL_SHORNAME_KEYWORD
import no.ssb.metadata.vardef.integrations.vardok.getValidDates
import no.ssb.metadata.vardef.integrations.vardok.mapVardokComment
import no.ssb.metadata.vardef.integrations.vardok.mapVardokStatisticalUnitToUnitTypes
import no.ssb.metadata.vardef.integrations.vardok.models.*
import no.ssb.metadata.vardef.models.LanguageStringType

@Prototype
@Introspected
interface VardokService {
    fun getVardokItem(id: String): VardokResponse?

    fun getVardokByIdAndLanguage(
        id: String,
        language: String,
    ): VardokResponse?

    fun fetchMultipleVardokItemsByLanguage(id: String): MutableMap<String, VardokResponse>

    fun createVarDefInputFromVarDokItems(varDokItems: Map<String, VardokResponse>): String {
       // checkVardokForMissingElements(varDokItems)
        val varDefInput = extractVardefInput(varDokItems)
        val mapper = ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        return mapper.writeValueAsString(varDefInput)
    }

    companion object {
        fun extractVardefInput(vardokItem: Map<String, VardokResponse>): VardefInput {
            val vardokItemNb = vardokItem["nb"] ?: throw MissingNbLanguageException()
            val comment = mapVardokComment(vardokItem)
            val classificationRelation = vardokItemNb.relations?.classificationRelation?.href
            val vardokShortname =
                vardokItemNb.variable
                    ?.dataElementName
                    ?.takeIf { it.isNotBlank() }
                    ?: (ILLEGAL_SHORNAME_KEYWORD + NanoId.generate(8))
            return VardefInput(
                name =
                    LanguageStringType(
                        vardokItemNb.common?.title,
                        vardokItem["nn"]?.common?.title,
                        vardokItem["en"]?.common?.title,
                    ),
                shortName = vardokShortname.lowercase(),
                definition =
                    LanguageStringType(
                        vardokItemNb.common?.description,
                        vardokItem["nn"]?.common?.description,
                        vardokItem["en"]?.common?.description,
                    ),
                validFrom = getValidDates(vardokItemNb).first,
                validUntil = getValidDates(vardokItemNb).second,
                unitTypes = mapVardokStatisticalUnitToUnitTypes(vardokItemNb),
                externalReferenceUri = vardokItemNb.variable?.externalDocument,
                comment =
                    LanguageStringType(
                        comment["nb"],
                        comment["nn"],
                        comment["en"],
                    ),
                containsSpecialCategoriesOfPersonalData = false,
                subjectFields = emptyList(),
                classificationReference = classificationRelation?.split("/")?.last(),
                contact = null,
                measurementType = null,
                relatedVariableDefinitionUris = emptyList(),
            )
        }
    }
}
