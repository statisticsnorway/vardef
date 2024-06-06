package no.ssb.metadata.klass

import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import io.micronaut.scheduling.annotation.Scheduled
import jakarta.inject.Inject
import org.slf4j.LoggerFactory
import java.text.SimpleDateFormat
import java.util.*

@Controller("/classifications")
@ExecuteOn(TaskExecutors.BLOCKING)
class ClassificationsController {

    @Inject
    lateinit var client: ClassificationsClient

    @Get()
    @Scheduled(cron = "0 25 10 * * ?")
    fun classifications(): HttpStatus  {
        LOG.info(
            "Check https response: {} {}",
            HttpStatus.OK,
            SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(
                Date(),
            ),
        )
        return HttpStatus.OK
    }

    @Get("/fetch")
    fun fetch(): HttpStatus {
        client.fetchData()
        return HttpStatus.OK
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(ClassificationsController::class.java)
    }
}
