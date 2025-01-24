package no.ssb.metadata.vardef.integrations.vardok.services

import io.micronaut.context.annotation.Prototype
import io.micronaut.core.annotation.Introspected
import io.viascom.nanoid.NanoId
import no.ssb.metadata.vardef.constants.ILLEGAL_SHORNAME_KEYWORD
import no.ssb.metadata.vardef.constants.VARDEF_SHORT_NAME_PATTERN
import no.ssb.metadata.vardef.integrations.vardok.convertions.getValidDates
import no.ssb.metadata.vardef.integrations.vardok.convertions.mapVardokComment
import no.ssb.metadata.vardef.integrations.vardok.convertions.mapVardokStatisticalUnitToUnitTypes
import no.ssb.metadata.vardef.integrations.vardok.convertions.mapVardokSubjectAreaToSubjectFiled
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

    fun createVardokVardefIdMapping(
        vardokId: String,
        vardefId: String,
    ): VardokVardefIdPair

    fun getVardefIdByVardokId(vardokId: String): String?

    companion object {
        private fun generateShortName() = "${ILLEGAL_SHORNAME_KEYWORD}${NanoId.generate(8)}".lowercase().replace("-", "_")

        private fun isValidShortName(name: String) = name.matches(Regex(VARDEF_SHORT_NAME_PATTERN))

        private fun processShortName(name: String?) =
            name
                ?.lowercase()
                ?.replace("""[-\s]""".toRegex(), "_")
                ?.takeIf { it.isNotBlank() && isValidShortName(it) }
                ?: generateShortName()

        fun extractVardefInput(vardokItem: Map<String, VardokResponse>): VardefInput {
            val vardokItemNb = vardokItem["nb"] ?: throw MissingNbLanguageException()
            val comment = mapVardokComment(vardokItem)
            val classificationRelation = vardokItemNb.relations?.classificationRelation?.href
            val vardokShortName = processShortName(vardokItemNb.variable?.dataElementName)

            return VardefInput(
                name =
                    LanguageStringType(
                        vardokItemNb.common?.title,
                        vardokItem["nn"]?.common?.title,
                        vardokItem["en"]?.common?.title,
                    ),
                shortName = vardokShortName,
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
                subjectFields = mapVardokSubjectAreaToSubjectFiled(vardokItemNb),
                classificationReference = classificationRelation?.split("/")?.last(),
                contact = null,
                measurementType = null,
                relatedVariableDefinitionUris = emptyList(),
            )
        }
    }
}
