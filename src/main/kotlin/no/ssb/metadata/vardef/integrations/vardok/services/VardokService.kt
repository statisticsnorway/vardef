package no.ssb.metadata.vardef.integrations.vardok.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import io.micronaut.context.annotation.Prototype
import io.micronaut.core.annotation.Introspected
import io.viascom.nanoid.NanoId
import no.ssb.metadata.vardef.constants.ILLEGAL_SHORNAME_KEYWORD
import no.ssb.metadata.vardef.constants.VARDEF_SHORT_NAME_PATTERN
import no.ssb.metadata.vardef.integrations.vardok.getValidDates
import no.ssb.metadata.vardef.integrations.vardok.mapVardokIdentifier
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

    fun createVarDefInputFromVarDokItems(varDokItems: MutableMap<String, VardokResponse>): String {
        checkVardokForMissingElements(varDokItems)
        val varDefInput = extractVardefInput(varDokItems)
        val mapper = ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        return mapper.writeValueAsString(varDefInput)
    }

    companion object {
        private fun generateShortName() = "${ILLEGAL_SHORNAME_KEYWORD}${NanoId.generate(8)}".lowercase().replace("-", "_")

        private fun isValidShortName(name: String) = name.matches(Regex(VARDEF_SHORT_NAME_PATTERN))

        private fun processShortName(name: String?) =
            name
                ?.lowercase()
                ?.replace("""[-\s]""".toRegex(), "_")
                ?.takeIf { it.isNotBlank() && isValidShortName(it) }
                ?: generateShortName()

        fun extractVardefInput(vardokItem: MutableMap<String, VardokResponse>): VardefInput {
            val vardokItemNb = vardokItem["nb"] ?: throw MissingNbLanguageException()
            val vardokId = mapVardokIdentifier(vardokItemNb)
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
                unitTypes = mapVardokStatisticalUnitToUnitTypes(vardokItemNb),
                externalReferenceUri = "https://www.ssb.no/a/xml/metadata/conceptvariable/vardok/$vardokId",
                containsSpecialCategoriesOfPersonalData = false,
                subjectFields = emptyList(),
                classificationReference = null,
                contact = null,
                measurementType = null,
                relatedVariableDefinitionUris = emptyList(),
            )
        }
    }
}
