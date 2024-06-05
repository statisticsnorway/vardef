package no.ssb.metadata.klass

import com.sun.net.httpserver.Authenticator
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import io.micronaut.scheduling.annotation.Scheduled
import org.slf4j.LoggerFactory
import java.text.SimpleDateFormat
import java.util.*

@Controller("/classifications")
@ExecuteOn(TaskExecutors.BLOCKING)
class ClassificationsController {

    @Get("https://data.ssb.no/api/klass/v1/classifications")
    @Scheduled(cron = "0 04 16 * * ?")
    fun getClassificationsResponse(): HttpStatus{
        LOG.info("Check https response: {} {}", HttpStatus.OK, SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(
            Date()
        ))
        return HttpStatus.OK
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(ClassificationsController::class.java)
    }
}