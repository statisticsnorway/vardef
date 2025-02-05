package no.ssb.metadata.vardef.migrations

import com.mongodb.client.model.Filters.*
import com.mongodb.client.model.Updates.set
import com.mongodb.reactivestreams.client.MongoDatabase
import io.mongock.api.annotations.ChangeUnit
import io.mongock.api.annotations.Execution
import io.mongock.api.annotations.RollbackExecution
import no.ssb.metadata.vardef.constants.GENERATED_CONTACT_KEYWORD
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono

@ChangeUnit(id = "set-default-value-to-contact", order = "002", author = "cbi")
class ContactIsMandatory {
    private val logger: Logger = LoggerFactory.getLogger(ContactIsMandatory::class.java)

    @Execution
    fun execution(mongoDatabase: MongoDatabase) {
        val filter =
            and(
                exists("contact"),
                eq(null),
            )
        val updater =
            mongoDatabase
                .getCollection("SavedVariableDefinition")
                .updateMany(
                    filter,
                    listOf(
                        set("contact.email", "$GENERATED_CONTACT_KEYWORD@epost.com"),
                        set("contact.title.nb", "$GENERATED_CONTACT_KEYWORD _tittel"),
                    ),
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
    fun rollback(mongoDatabase: MongoDatabase) {
        val filter =
            and(
                exists("contact"),
                eq(null),
            )
        val updater =
            mongoDatabase
                .getCollection("SavedVariableDefinition")
                .updateMany(filter, set("contact.email", "$GENERATED_CONTACT_KEYWORD@epost.com"))
        Mono.from(updater).block()
    }
}
