package no.ssb.metadata

import com.mongodb.assertions.Assertions.assertFalse
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MongoDBContainer {
    // @Inject private val mongoDBContainer: MongoDBContainer

    /*companion object {

        private lateinit var mongoDBContainer: MongoDBContainer

        @BeforeAll
        @JvmStatic
        fun setup() {
            mongoDBContainer = MongoDBContainer("mongo:4.0.10")
            mongoDBContainer.start()
            System.setProperty("mongodb.uri", mongoDBContainer.replicaSetUrl)

            // Initialize the MongoClient and insert test data
            val mongoClient = MongoClients.create(mongoDBContainer.replicaSetUrl)
            val database = mongoClient.getDatabase("vardef")
            val collection = database.getCollection("testCollection")
            collection.insertOne(Document("field", "expected result"))
            mongoClient.close()
        }

        @AfterAll
        @JvmStatic
        fun teardown() {
            mongoDBContainer.stop()
        }
    }
*/
    @Test
    fun testSomething() {
        assertFalse(false)
    }
}
