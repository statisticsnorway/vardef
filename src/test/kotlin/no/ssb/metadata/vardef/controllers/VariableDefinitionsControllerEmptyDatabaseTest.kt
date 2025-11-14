package no.ssb.metadata.vardef.controllers

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.restassured.RestAssured
import io.restassured.RestAssured.oauth2
import io.restassured.filter.log.RequestLoggingFilter
import io.restassured.filter.log.ResponseLoggingFilter
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import jakarta.inject.Inject
import no.ssb.metadata.vardef.integrations.vardok.repositories.VardokIdMappingRepository
import no.ssb.metadata.vardef.repositories.VariableDefinitionRepository
import no.ssb.metadata.vardef.utils.*
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class VariableDefinitionsControllerEmptyDatabaseTest {
    @Inject
    lateinit var variableDefinitionRepository: VariableDefinitionRepository

    @Inject
    lateinit var vardokIdMappingRepository: VardokIdMappingRepository

    @BeforeEach
    fun setUp() {
        variableDefinitionRepository.deleteAll()
        vardokIdMappingRepository.deleteAll()
    }

    init {
        if (RestAssured.filters() == null) {
            RestAssured.filters(RequestLoggingFilter(), ResponseLoggingFilter())
        }
        RestAssured.authentication = oauth2(JwtTokenHelper.jwtTokenSigned().parsedString)
    }

    @Test
    fun `access empty database`(spec: RequestSpecification) {
        spec
            .`when`()
            .contentType(ContentType.JSON)
            .get("/variable-definitions")
            .then()
            .statusCode(200)
            .body("", empty<List<Any>>())
    }

    @Test
    fun `access empty vardok vardef mapping `(spec: RequestSpecification) {
        spec
            .`when`()
            .contentType(ContentType.JSON)
            .get("/vardok-migration")
            .then()
            .statusCode(200)
            .body("", empty<List<Any>>())
    }
}
