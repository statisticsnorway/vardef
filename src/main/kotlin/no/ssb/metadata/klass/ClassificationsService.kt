package no.ssb.metadata.klass

import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import java.net.http.HttpResponse

@Singleton
class ClassificationsService(@Inject private val classificationsClient: ClassificationsClient) {

    fun getData() {
        try {
            val response: HttpResponse<Any> = classificationsClient.fetchData()
            LOG.info("Response {}", response)
            // Here you can use the response, e.g., logging it
            println("Response received: ${response.body()}")
        }
        catch (e: Exception) {
            // Handle the exception
            println("Error occurred: ${e.message}")
        }
    }
    companion object {
        private val LOG = LoggerFactory.getLogger(ClassificationsService::class.java)
    }
}