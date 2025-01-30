package no.ssb.metadata.vardef.migrations

import com.mongodb.reactivestreams.client.MongoClient
import io.micronaut.context.annotation.Requires
import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.context.event.StartupEvent
import io.mongock.driver.mongodb.reactive.driver.MongoReactiveDriver
import io.mongock.runner.standalone.MongockStandalone
import jakarta.inject.Singleton
import no.ssb.metadata.vardef.repositories.MONGO_DB_NAME_VARDEF
import org.slf4j.LoggerFactory

@Singleton
@Requires(beans = [MongoClient::class])
class MongockRunner(
    private val mongoClient: MongoClient,
) : ApplicationEventListener<StartupEvent> {
    val logger = LoggerFactory.getLogger(MongockRunner::class.java)

    override fun onApplicationEvent(event: StartupEvent?) {
        logger.info("Connected to DB: ${mongoClient.getDatabase(MONGO_DB_NAME_VARDEF).name}")

        val mongock =
            MongockStandalone
                .builder()
                .setDriver(MongoReactiveDriver.withDefaultLock(mongoClient, MONGO_DB_NAME_VARDEF))
                .addMigrationScanPackage("no.ssb.metadata.vardef.migrations")
                .setTransactional(false)
                .buildRunner()

        mongock.execute()
        logger.info("All MongoDB migrations are completed")
    }
}
