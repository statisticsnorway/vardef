package no.ssb.metadata.vardef.migrations

import com.mongodb.client.model.Filters.*
import com.mongodb.client.model.Updates.set
import com.mongodb.reactivestreams.client.MongoDatabase
import io.mongock.api.annotations.Execution
import io.mongock.api.annotations.RollbackExecution
import no.ssb.metadata.vardef.constants.GENERATED_CONTACT_KEYWORD
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono

// @ChangeUnit(id = "set-contact-to-null", order = "003", author = "cbi")
class SetContactToNull {
    private val logger: Logger = LoggerFactory.getLogger(SetContactToNull::class.java)

    @Execution
    fun execution(mongoDatabase: MongoDatabase) {
        val filter =
            and(
                exists("contact"),
                eq("contact.title.nb", GENERATED_CONTACT_KEYWORD),
            )
        val updater =
            mongoDatabase
                .getCollection("SavedVariableDefinition")
                .updateMany(
                    filter,
                    set("contact", null),
                )
        Mono.from(updater).block().also { updateResult ->
            if (updateResult != null) {
                logger.info("Migration successful, updated ${updateResult.modifiedCount} documents")
            } else {
                logger.error("Migration result was null!")
            }
        }
    }

    @RollbackExecution
    fun rollback() {
    }
}
