import com.mongodb.ConnectionString
import org.testcontainers.containers.GenericContainer


fun populateDB() {

    val mongoContainer: GenericContainer<*> = GenericContainer("mongo:latest")
    val connectionString = ConnectionString("mongodb://${MONGO_USER}:${MONGO_PASSWORD}@localhost:${mongoContainer.getMappedPort(MONGO_PORT)}/?authSource=admin&authMechanism=SCRAM-SHA-1")



}