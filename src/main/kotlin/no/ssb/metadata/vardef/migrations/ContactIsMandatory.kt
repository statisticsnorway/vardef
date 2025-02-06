package no.ssb.metadata.vardef.migrations

import com.mongodb.client.model.Filters.*
import com.mongodb.client.model.Updates.combine
import com.mongodb.client.model.Updates.set
import com.mongodb.reactivestreams.client.MongoDatabase
import io.mongock.api.annotations.ChangeUnit
import io.mongock.api.annotations.Execution
import io.mongock.api.annotations.RollbackExecution
import no.ssb.metadata.vardef.constants.GENERATED_CONTACT_KEYWORD
import org.bson.Document
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono

@ChangeUnit(id = "set-default-value-to-null-contact", order = "002", author = "cbi")
class ContactIsMandatory {
    private val logger: Logger = LoggerFactory.getLogger(ContactIsMandatory::class.java)

    @Execution
    fun execution(mongoDatabase: MongoDatabase) {
        // Step 1:  filter where contact is null and create a new document
        val updateContact =
            Mono.from(
                mongoDatabase.getCollection("SavedVariableDefinition")
                    .updateMany(
                        eq("contact", null),
                        set("contact", Document()),
                    ),
            )

        // Step 2: filter title is null and create new document
        val updateTitle =
            Mono.from(
                mongoDatabase.getCollection("SavedVariableDefinition")
                    .updateMany(
                        eq("contact.title", null),
                        set("contact.title", Document()),
                    ),
            )

        // Step 3: set value to title and email
        val updateFields =
            Mono.from(
                mongoDatabase.getCollection("SavedVariableDefinition")
                    .updateMany(
                        and(
                            exists("contact", true),
                            or(
                                eq("contact.title", null),
                                eq("contact.email", null),
                            ),
                        ),
                        combine(
                            set("contact.title.nb", GENERATED_CONTACT_KEYWORD),
                            set("contact.email", "${GENERATED_CONTACT_KEYWORD}@email.com"),
                        ),
                    ),
            )

        // All three updates
        updateContact
            .then(updateTitle)
            .then(updateFields)
            .block()
            .also { updateResult ->
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
