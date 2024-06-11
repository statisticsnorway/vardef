package no.ssb.metadata.vardef.integrations.klass.service

import io.micronaut.http.HttpStatus
import no.ssb.metadata.vardef.integrations.klass.models.KlassApiResponse
import org.slf4j.LoggerFactory

class KlassApiRequests(private val klassApiService: KlassApiService) {

    fun getClassifications(): KlassApiResponse? {
        if (klassApiService.klassApiResponse == null) {
            LOG.info("Request Klass Api")
            klassApiService.callKlassApiJob(klassApiService)
            if (klassApiService.klassApiJob().status == HttpStatus.INTERNAL_SERVER_ERROR) {
                LOG.warn("Klass Api is unavailable")
                return null
            } else {
            LOG.info("Refreshing Klass Api cache")
            return klassApiService.klassApiResponse
             }
        }
        LOG.info("Fetching from cache")
        return klassApiService.klassApiResponse!!
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(KlassApiRequests::class.java)
    }
}