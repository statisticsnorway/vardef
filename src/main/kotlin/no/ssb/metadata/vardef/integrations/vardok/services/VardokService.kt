package no.ssb.metadata.vardef.integrations.vardok.services

import io.micronaut.context.annotation.Prototype
import io.micronaut.core.annotation.Introspected
import io.viascom.nanoid.NanoId
import no.ssb.metadata.vardef.constants.GENERATED_CONTACT_KEYWORD
import no.ssb.metadata.vardef.constants.ILLEGAL_SHORTNAME_KEYWORD
import no.ssb.metadata.vardef.constants.VARDEF_SHORT_NAME_PATTERN
import no.ssb.metadata.vardef.integrations.vardok.conversions.*
import no.ssb.metadata.vardef.integrations.vardok.models.*
import no.ssb.metadata.vardef.models.Contact
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

    fun getVardokVardefIdMapping(): List<VardokVardefIdPairResponse>

    fun getVardefIdByVardokId(vardokId: String): String?

    fun getVardokIdByVardefId(vardokId: String): String?

    fun isAlreadyMigrated(vardokId: String): Boolean

    fun isDuplicate(name: String): Boolean

    companion object {
        fun generateShortName() = "${ILLEGAL_SHORTNAME_KEYWORD}${NanoId.generate(8)}".lowercase().replace("-", "_")

        private fun isValidShortName(name: String) = name.matches(Regex(VARDEF_SHORT_NAME_PATTERN))

        private fun processShortName(name: String?) =
            name
                ?.lowercase()
                ?.replace("""[-\s]""".toRegex(), "_")
                ?.takeIf { it.isNotBlank() && isValidShortName(it) }
                ?: generateShortName()

        fun extractVardefInput(vardokItem: Map<String, VardokResponse>): VardefInput {
            val vardokItemPrimary = vardokItem["nb"] ?: vardokItem["nn"] ?: throw MissingPrimaryLanguageException()
            val comment = mapVardokComment(vardokItem)
            val classificationRelation = vardokItemPrimary.relations?.classificationRelation?.href
            val vardokShortName = processShortName(vardokItemPrimary.variable?.dataElementName)
            // Add title value to primary language field
            val title =
                LanguageStringType(null, null, null).apply {
                    if (vardokItemPrimary.xmlLang == "nb") {
                        nb = "${GENERATED_CONTACT_KEYWORD}_tittel"
                    } else if (vardokItemPrimary.xmlLang == "nn") {
                        nn = "${GENERATED_CONTACT_KEYWORD}_tittel"
                    }
                }
            return VardefInput(
                name =
                    LanguageStringType(
                        vardokItem["nb"]?.common?.title,
                        vardokItem["nn"]?.common?.title,
                        vardokItem["en"]?.common?.title,
                    ),
                shortName = vardokShortName,
                definition =
                    LanguageStringType(
                        vardokItem["nb"]?.common?.description,
                        vardokItem["nn"]?.common?.description,
                        vardokItem["en"]?.common?.description,
                    ),
                validFrom = getValidDates(vardokItemPrimary).first,
                validUntil = getValidDates(vardokItemPrimary).second,
                unitTypes = mapVardokStatisticalUnitToUnitTypes(vardokItemPrimary),
                externalReferenceUri = mapExternalDocumentToUri(vardokItemPrimary),
                comment =
                    if (comment.values.any { !it.isNullOrEmpty() }) {
                        LanguageStringType(
                            comment["nb"],
                            comment["nn"],
                            comment["en"],
                        )
                    } else {
                        null
                    },
                containsSpecialCategoriesOfPersonalData = false,
                subjectFields = mapVardokSubjectAreaToSubjectField(vardokItemPrimary).map { it.code },
                classificationReference = classificationRelation?.split("/")?.last(),
                contact =
                    Contact(
                        title = title,
                        email = "$GENERATED_CONTACT_KEYWORD@epost.com",
                    ),
                measurementType = null,
                relatedVariableDefinitionUris = mapConceptVariableRelations(vardokItemPrimary),
            )
        }
    }
}
