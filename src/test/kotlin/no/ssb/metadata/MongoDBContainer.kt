package no.ssb.metadata

import com.mongodb.assertions.Assertions.assertFalse
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.containers.MongoDBContainer

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MongoDBContainer {

    //@Inject private val mongoDBContainer: MongoDBContainer
    companion object {
        private lateinit var mongoDBContainer: MongoDBContainer

        @BeforeAll
        @JvmStatic
        fun setup() {
            mongoDBContainer = MongoDBContainer("mongo:4.0.10")
            mongoDBContainer.start()
            System.setProperty("mongodb.uri", mongoDBContainer.replicaSetUrl)
        }

        @AfterAll
        @JvmStatic
        fun teardown() {
            mongoDBContainer.stop()
        }
    }

    @Test
    fun testSomething() {
        // Your test logic here
        assertFalse(false)
    }
}
