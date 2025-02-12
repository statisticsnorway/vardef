package no.ssb.metadata.vardef.migrations

import com.mongodb.reactivestreams.client.MongoDatabase
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ContactIsMandatoryTest {
    private lateinit var mongoDatabase: MongoDatabase

    @BeforeEach
    fun createMocks() {
        mongoDatabase = mockk<MongoDatabase>(relaxed = true)
    }

    @Test
    fun `mock test execution`() {
        println("STARTED TEST")
        ContactIsMandatory().execution(mongoDatabase)
    }

//    @Primary
//    @MockBean(MongoDatabase::class)
//    fun mockMongoDatabase(): MongoDatabase {
//        println("CREATING MOCK")
//        val mockMongoDatabase = mockk<MongoDatabase>(relaxed = true)
//        return mockMongoDatabase
//    }
}
