package no.ssb.metadata.vardef.integrations.vardok

import jakarta.inject.Singleton
import org.slf4j.LoggerFactory

@Singleton
open class VarDokApiService(private val varDokClient: VarDokClient) {
    private val logger = LoggerFactory.getLogger(VarDokApiService::class.java)

    open fun getVarDokResponse(): FIMD? {
        return try {
            logger.info("Retrieving definition from vardok")
            varDokClient.fetchVarDok()
        } catch (e: Exception) {
            logger.warn("Error while fetching vardok", e)
            null
        }
    }

    open fun getVarDokItem(id: String): FIMD? {
        return try {
            logger.info("Retrieving definition by id from vardok")
            varDokClient.fetchVarDokById(id)
        } catch (e: Exception) {
            logger.warn("Error while fetching vardok item", e)
            null
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
            // if language in
            logger.info("Retrieving $id by $language")
            varDokClient.fetchVarDokByIdAndLanguage(id, language)
        } catch (e: Exception) {
            logger.warn("Error while fetching vardok by id and language", e)
            null
        }
    }
}
