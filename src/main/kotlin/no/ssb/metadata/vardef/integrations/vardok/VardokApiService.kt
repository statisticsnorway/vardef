package no.ssb.metadata.vardef.integrations.vardok

import jakarta.inject.Singleton
import org.slf4j.LoggerFactory

@Singleton
open class VardokApiService(private val varDokClient: VarDokClient) {
    private val logger = LoggerFactory.getLogger(VardokApiService::class.java)

    open fun getVardokResponse(): FIMD? {
        return try {
            logger.info("Retrieving one definition from varDok")
            varDokClient.fetchVarDok()
        } catch (e: Exception) {
            logger.warn("Error while fetching varDok item", e)
            null
        }
    }
}