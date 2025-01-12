package no.ssb.metadata.vardef.integrations.vardok.services

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import jakarta.inject.Singleton
import no.ssb.metadata.vardef.integrations.vardok.client.VardokClient
import no.ssb.metadata.vardef.integrations.vardok.models.*
import org.slf4j.LoggerFactory

@Singleton
open class VardokApiService(
    private val vardokClient: VardokClient,
) : VardokService {
    private val logger = LoggerFactory.getLogger(VardokApiService::class.java)

    val xmlMapper = XmlMapper().registerKotlinModule()

    override fun getVardokItem(id: String): VardokResponse? {
        try {
            logger.info("Retrieving definition by $id from vardok")
            val res = vardokClient.fetchVardokById(id)
            return xmlMapper.readValue(res, VardokResponse::class.java)
        } catch (e: Exception) {
            logger.warn("$id is not found. Exception message: ${e.message}")
            throw VardokNotFoundException(id)
        }
    }

    override fun getVardokByIdAndLanguage(
        id: String,
        language: String,
    ): VardokResponse? {
        try {
            logger.info("Retrieving $id by $language")
            val res = vardokClient.fetchVardokByIdAndLanguage(id, language)
            return xmlMapper.readValue(res, VardokResponse::class.java)
        } catch (e: Exception) {
            logger.warn("Error while fetching vardok by id and language", e)
            throw (HttpStatusException(HttpStatus.NOT_FOUND, "Id $id in language: $language not found"))
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
        return responseMap
    }
}
