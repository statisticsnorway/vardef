package no.ssb.metadata.vardef.integrations.vardok

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import jakarta.inject.Singleton
import no.ssb.metadata.vardef.constants.VARDEF_SHORT_NAME_PATTERN
import org.slf4j.LoggerFactory

@Singleton
open class VarDokService(
    private val varDokClient: VarDokClient,
) {
    private val logger = LoggerFactory.getLogger(VarDokService::class.java)

    open fun getVarDokItem(id: String): VardokResponse? {
        return try {
            logger.info("Retrieving definition by $id from vardok")
            varDokClient.fetchVarDokById(id)
        } catch (e: Exception) {
            logger.warn("$id is not found. Exception message: ${e.message}")
            throw VardokNotFoundException(id)
        }
    }

    open fun getVardokByIdAndLanguage(
        id: String,
        language: String,
    ): VardokResponse? {
        return try {
            logger.info("Retrieving $id by $language")
            varDokClient.fetchVarDokByIdAndLanguage(id, language)
        } catch (e: Exception) {
            logger.warn("Error while fetching vardok by id and language", e)
            throw (HttpStatusException(HttpStatus.NOT_FOUND, "Id $id in language: $language not found"))
        }
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
        val vardokId = varDokItems["nb"]?.id?.substringAfterLast(":").toString()
        val varDefInput = toVarDefFromVarDok(varDokItems)

        if (varDefInput.shortName?.matches(VARDEF_SHORT_NAME_PATTERN.toRegex()) == false) {
            throw IllegalShortNameException(vardokId)
        }

        val mapper = ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        return mapper.writeValueAsString(varDefInput)
    }
}
