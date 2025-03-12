package no.ssb.metadata.vardef.integrations.vardok.services

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.micronaut.http.client.exceptions.HttpClientResponseException
import jakarta.inject.Inject
import jakarta.inject.Singleton
import no.ssb.metadata.vardef.integrations.vardok.client.VardokClient
import no.ssb.metadata.vardef.integrations.vardok.models.*
import no.ssb.metadata.vardef.integrations.vardok.repositories.VardokIdMappingRepository
import no.ssb.metadata.vardef.repositories.VariableDefinitionRepository
import org.slf4j.LoggerFactory

@Singleton
open class VardokApiService(
    private val vardokClient: VardokClient,
    private val vardokIdMappingRepository: VardokIdMappingRepository,
) : VardokService {
    private val logger = LoggerFactory.getLogger(VardokApiService::class.java)

    @Inject
    lateinit var variableDefinitionRepository: VariableDefinitionRepository

    override fun isDuplicate(name: String): Boolean {
        return variableDefinitionRepository.existsByShortName(name)
    }

    private val xmlMapper = XmlMapper().registerKotlinModule()

    override fun getVardokItem(id: String): VardokResponse? {
        try {
            logger.info("Retrieving definition by $id from vardok")
            val response = vardokClient.fetchVardokById(id)
            return xmlMapper.readValue(response, VardokResponse::class.java)
        } catch (e: Exception) {
            if (e is HttpClientResponseException) {
                logger.warn("$id is not found. Exception message: ${e.message}")
                throw VardokNotFoundException("Vardok id $id not found")
            }
            logger.warn("Unexpected exception for $id. Exception message: ${e.message}")
            throw e
        }
    }

    override fun getVardokByIdAndLanguage(
        id: String,
        language: String,
    ): VardokResponse? {
        try {
            logger.info("Retrieving $id by $language")
            val response = vardokClient.fetchVardokByIdAndLanguage(id, language)
            return xmlMapper.readValue(response, VardokResponse::class.java)
        } catch (e: Exception) {
            if (e is HttpClientResponseException) {
                logger.warn("Error while fetching vardok by id and language", e)
                throw (VardokNotFoundException("Id $id in language: $language not found"))
            }
            logger.warn("Unexpected exception for $id in language $language. Exception message: ${e.message}")
            throw e
        }
    }

    override fun fetchMultipleVardokItemsByLanguage(id: String): MutableMap<String, VardokResponse> {
        val result = getVardokItem(id)
        val responseMap = mutableMapOf<String, VardokResponse>()
        result?.let {
            responseMap["nb"] = it
        }
        result?.otherLanguages?.split(";")?.filter { it.isNotEmpty() }?.forEach { l ->
            getVardokByIdAndLanguage(id, l)?.let { responseMap[l] = it }
        }
        if (result?.variable?.dataElementName?.let { isDuplicate(it) } == true) {
            result.variable.dataElementName = VardokService.generateShortName()
            logger.info(
                "Shortname for vardok id ${result.id.split(":").last()} was duplicate and new shortname " +
                    "${result.variable.dataElementName} generated",
            )
        }
        return responseMap
    }

    override fun createVardokVardefIdMapping(
        vardokId: String,
        vardefId: String,
    ): VardokVardefIdPair = vardokIdMappingRepository.save(VardokVardefIdPair(vardokId, vardefId))

    override fun getVardefIdByVardokId(vardokId: String): String? = vardokIdMappingRepository.getVardefIdByVardokId(vardokId)

    override fun isAlreadyMigrated(vardokId: String): Boolean = vardokIdMappingRepository.existsByVardokId(vardokId)
}
