package no.ssb.metadata.vardef.integrations.vardok.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import jakarta.inject.Singleton
import no.ssb.metadata.vardef.integrations.vardok.models.*
import no.ssb.metadata.vardef.models.LanguageStringType
import org.slf4j.LoggerFactory

@Singleton
open class VarDokApiService(
    private val varDokClient: VarDokClient,
) : VarDokService {
    private val logger = LoggerFactory.getLogger(VarDokApiService::class.java)

    override fun getVarDokItem(id: String): VardokResponse? =
        try {
            logger.info("Retrieving definition by $id from vardok")
            varDokClient.fetchVarDokById(id)
        } catch (e: Exception) {
            logger.warn("$id is not found. Exception message: ${e.message}")
            throw VardokNotFoundException(id)
        }

    override fun getVardokByIdAndLanguage(
        id: String,
        language: String,
    ): VardokResponse? =
        try {
            logger.info("Retrieving $id by $language")
            varDokClient.fetchVarDokByIdAndLanguage(id, language)
        } catch (e: Exception) {
            logger.warn("Error while fetching vardok by id and language", e)
            throw (HttpStatusException(HttpStatus.NOT_FOUND, "Id $id in language: $language not found"))
        }

    fun fetchMultipleVarDokItemsByLanguage(id: String): MutableMap<String, VardokResponse> {
        val result = getVarDokItem(id)
        val responseMap = mutableMapOf<String, VardokResponse>()
        result?.let {
            responseMap["nb"] = it
        }

        result?.otherLanguages?.split(";")?.filter { it.isNotEmpty() }?.forEach { l ->
            getVardokByIdAndLanguage(id, l)?.let { responseMap[l] = it }
        }

        return responseMap
    }

    fun createVarDefInputFromVarDokItems(varDokItems: MutableMap<String, VardokResponse>): String {
        checkVardokForMissingElements(varDokItems)
        val varDefInput = extractVardefInput(varDokItems)

        val mapper = ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        return mapper.writeValueAsString(varDefInput)
    }

    companion object {
        fun extractVardefInput(vardokItem: MutableMap<String, VardokResponse>): VardefInput {
            val vardokItemNb = vardokItem["nb"] ?: throw MissingNbLanguageException()
            val vardokId = mapVardokIdentifier(vardokItemNb)

            return VardefInput(
                name =
                    LanguageStringType(
                        vardokItemNb.common?.title,
                        vardokItem["nn"]?.common?.title,
                        vardokItem["en"]?.common?.title,
                    ),
                shortName = vardokItemNb.variable?.dataElementName?.lowercase(),
                definition =
                    LanguageStringType(
                        vardokItemNb.common?.description,
                        vardokItem["nn"]?.common?.description,
                        vardokItem["en"]?.common?.description,
                    ),
                validFrom = getValidDates(vardokItemNb).first,
                unitTypes = mapVardokStatisticalUnitToUnitTypes(vardokItemNb),
                externalReferenceUri = "https://www.ssb.no/a/xml/metadata/conceptvariable/vardok/$vardokId",
                containsSensitivePersonalInformation = false,
                subjectFields = emptyList(),
                classificationReference = null,
                contact = null,
                measurementType = null,
                relatedVariableDefinitionUris = emptyList(),
            )
        }
    }
}
