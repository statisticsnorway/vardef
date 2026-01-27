
package no.ssb.metadata.vardef.migrations

import com.mongodb.client.model.Filters.*
import com.mongodb.reactivestreams.client.MongoDatabase
import io.mongock.api.annotations.ChangeUnit
import io.mongock.api.annotations.Execution
import org.bson.Document
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono

@ChangeUnit(
    id = "measurement-type-use-level-one",
    order = "003",
    author = "cbi",
)
class MeasurementTypeUseLevelOne {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Execution
    fun execution(mongoDatabase: MongoDatabase) {
        val result =
            Mono.from(
                mongoDatabase
                    .getCollection("SavedVariableDefinition")
                    .updateMany(
                        and(
                            exists("measurementType"),
                            regex("measurementType", "\\."),
                        ),
                        listOf(
                            Document(
                                "\$set",
                                Document(
                                    "measurementType",
                                    Document(
                                        "\$arrayElemAt",
                                        listOf(
                                            Document("\$split", listOf("\$measurementType", ".")),
                                            0,
                                        ),
                                    ),
                                ),
                            ),
                        ),
                    ),
            ).block()

        if (result == null) {
            logger.error("MeasurementType migration result was null!")
        } else {
            logger.info(
                "MeasurementType migration successful, updated ${result.modifiedCount} documents",
            )
        }
    }
}

