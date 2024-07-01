package no.ssb.metadata.vardef.integrations.vardok

import jakarta.inject.Singleton
import org.slf4j.LoggerFactory

@Singleton
open class VarDokApiService(private val varDokClient: VarDokClient) {
    private val logger = LoggerFactory.getLogger(VarDokApiService::class.java)

    open fun getVarDokResponse(): FIMD? {
        return try {
            logger.info("Retrieving definition from varDok")
            varDokClient.fetchVarDok()
        } catch (e: Exception) {
            logger.warn("Error while fetching varDok", e)
            null
        }
    }

    open fun getVarDokItem(id: String): FIMD? {
        return try {
            logger.info("Retrieving definition by id from varDok")
            varDokClient.fetchVarDokById(id)
        } catch (e: Exception) {
            logger.warn("Error while fetching varDok item", e)
            null
        }
    }

}