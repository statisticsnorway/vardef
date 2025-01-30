package no.ssb.metadata.vardef.migrations

import com.mongodb.reactivestreams.client.MongoDatabase
import io.mongock.api.annotations.ChangeUnit
import io.mongock.api.annotations.Execution
import io.mongock.api.annotations.RollbackExecution
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.runBlocking
import org.bson.BsonDocument
import org.slf4j.LoggerFactory

@ChangeUnit(id = "mongock-test", order = "001", author = "mmwinther")
class MongockTestChange {
    val logger = LoggerFactory.getLogger(MongockTestChange::class.java)

    /** This is the method with the migration code  */
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

    @RollbackExecution
    fun rollback(mongoDatabase: MongoDatabase) {
        mongoDatabase.getCollection("SavedVariableDefinition").deleteMany(BsonDocument())
    }
}
