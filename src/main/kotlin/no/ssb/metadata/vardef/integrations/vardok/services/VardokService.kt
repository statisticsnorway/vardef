package no.ssb.metadata.vardef.integrations.vardok.services

import io.micronaut.context.annotation.Prototype
import io.micronaut.core.annotation.Introspected
import io.viascom.nanoid.NanoId
import no.ssb.metadata.vardef.constants.GENERATED_CONTACT_KEYWORD
import no.ssb.metadata.vardef.constants.ILLEGAL_SHORTNAME_KEYWORD
import no.ssb.metadata.vardef.constants.VARDEF_SHORT_NAME_PATTERN
import no.ssb.metadata.vardef.integrations.vardok.convertions.*
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

    fun getVardefIdByVardokId(vardokId: String): String?

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
                externalReferenceUri = mapExternalDocumentToUri(vardokItemNb),
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
                subjectFields = mapVardokSubjectAreaToSubjectFiled(vardokItemNb),
                classificationReference = classificationRelation?.split("/")?.last(),
                contact =
                    Contact(
                        LanguageStringType("${GENERATED_CONTACT_KEYWORD}_tittel", null, null),
                        "$GENERATED_CONTACT_KEYWORD@epost.com",
                    ),
                measurementType = null,
                relatedVariableDefinitionUris = mapConceptVariableRelations(vardokItemNb),
            )
        }
    }
}
