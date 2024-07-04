package no.ssb.metadata.vardef.integrations.vardok

import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import jakarta.inject.Singleton
import no.ssb.metadata.vardef.models.InputVariableDefinition
import org.slf4j.LoggerFactory

@Singleton
open class VarDokApiService(
    private val varDokClient: VarDokClient,
) {
    private val logger = LoggerFactory.getLogger(VarDokApiService::class.java)

    open fun getVarDokItem(id: String): FIMD? {
        return try {
            logger.info("Retrieving definition by id from vardok")
            val result = varDokClient.fetchVarDokById(id)
            if(result.variable?.dataElementName.isNullOrBlank() or result.dc?.valid.isNullOrBlank()) {
                return null
            }
            result
        } catch (e: Exception) {
            logger.warn("Id is not valid")
            throw(HttpStatusException(HttpStatus.NO_CONTENT,"Id not found"))
            //burde returnere null
        }
    }

    open fun getListOfVardokById(vardokIdList: List<String>): List<FIMD?> {
        return try {
            logger.info("Retrieving multiple definitions from vardok")
            val vardokList = vardokIdList.map { getVarDokItem(it) }
            vardokList
        } catch (e: Exception) {
            logger.warn("Error while fetching list of vardok", e)
            emptyList()
        }
    }

    open fun getVardokByIdAndLanguage(
        id: String,
        language: String,
    ): FIMD? {
        return try {
            logger.info("Retrieving $id by $language")
            varDokClient.fetchVarDokByIdAndLanguage(id, language)
        } catch (e: Exception) {
            logger.warn("Error while fetching vardok by id and language", e)
            null
        }
    }

    fun fetchMultipleVarDokItemsByLanguage(id: String): MutableMap<String, FIMD> {
        val result = getVarDokItem(id)
        val responseMap = mutableMapOf<String, FIMD>()
        result?.let {
            responseMap["nb"] = it
        }

        result?.otherLanguages?.split(";")?.filter { it.isNotEmpty() }?.forEach { l ->
            getVardokByIdAndLanguage(id, l)?.let { responseMap[l] = it }
        }

        return responseMap
    }

    fun createVarDefInputFromVarDokItems(varDokItems: MutableMap<String, FIMD>): InputVariableDefinition {
        val varDefInput = toVarDefFromVarDok(varDokItems)

        // TODO Consider if we should skip if there is no date
//        if (varDefInput.validFrom == null) {
//            varDefInput.validFrom = LocalDate.now().toString()
//        }

        // val mapper = ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)

        return varDefInput
    }
}
