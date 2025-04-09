package no.ssb.metadata.vardef.migrations

import com.mongodb.reactivestreams.client.MongoDatabase
import io.mongock.api.annotations.ChangeUnit
import io.mongock.api.annotations.Execution
import io.mongock.api.annotations.RollbackExecution
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Mongock example migration
 *
 * Demonstrate that Mongock is configured with a migration which accesses the database
 * but does not change any data.
 */
@ChangeUnit(id = "mongock-example", order = "001", author = "mmwinther")
class MongockExampleMigration {
    private val logger: Logger = LoggerFactory.getLogger(MongockExampleMigration::class.java)

    @Execution
    fun execution(mongoDatabase: MongoDatabase) {
        val numberDocuments: Long?
        runBlocking {
            numberDocuments =
                mongoDatabase
                    .getCollection("SavedVariableDefinition")
                    .countDocuments()
                    .awaitFirst()
        }
        logger.info("Number of documents = $numberDocuments")
    }

    /**
     * Currently we're not making any changes so nothing to do for rollback
     */
    @RollbackExecution
    fun rollback() {
        // Intentionally left blank: no state changes to undo in current implementation
    }
}
