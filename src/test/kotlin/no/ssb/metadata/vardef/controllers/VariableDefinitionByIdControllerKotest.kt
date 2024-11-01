package no.ssb.metadata.vardef.controllers

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.datatest.withData
import io.micronaut.http.HttpStatus
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.extensions.kotest5.MicronautKotest5Extension
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import io.restassured.RestAssured
import io.restassured.RestAssured.oauth2
import io.restassured.filter.log.RequestLoggingFilter
import io.restassured.filter.log.ResponseLoggingFilter
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import no.ssb.metadata.vardef.repositories.VariableDefinitionRepository
import no.ssb.metadata.vardef.services.PatchesService
import no.ssb.metadata.vardef.services.VariableDefinitionService
import no.ssb.metadata.vardef.utils.*
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matchers.containsString
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Suppress("unused")
object ProjectConfig : AbstractProjectConfig() {
    override fun extensions() = listOf(MicronautKotest5Extension)
}

data class DateTestCase(
    val dateOfValidity: LocalDate?,
    val expectedStatus: HttpStatus,
    val expectedValidFrom: LocalDate?,
    val expectedValidUntil: LocalDate?,
)

@MicronautTest
class VariableDefinitionByIdControllerKotest(
    private val embeddedServer: EmbeddedServer,
    private val patches: PatchesService,
    private val repository: VariableDefinitionRepository,
) : DescribeSpec(
        {
            beforeAny {
                println("BeforeAny")
                repository.deleteAll()
                ALL_INCOME_TAX_PATCHES.forEach { patches.create(it) }
                patches.create(DRAFT_BUS_EXAMPLE)
                patches.create(SAVED_DRAFT_DEADWEIGHT_EXAMPLE)
                patches.create(SAVED_DEPRECATED_VARIABLE_DEFINITION)
                patches.create(SAVED_INTERNAL_VARIABLE_DEFINITION)
                println("Patches: ${repository.findAll().map { "\n${it.definitionId} - ${it.patchId}" }}")
            }
            beforeSpec {
                RestAssured.port = embeddedServer.port
                RestAssured.replaceFiltersWith(RequestLoggingFilter(), ResponseLoggingFilter())
                RestAssured.authentication = oauth2(JwtTokenHelper.jwtTokenSigned().parsedString)
            }
            describe("get variable definition") {
                it("malformed id") {
                    When {
                        get("/variable-definitions/MALFORMED_ID")
                    } Then {
                        statusCode(HttpStatus.BAD_REQUEST.code)
                        body(ERROR_MESSAGE_JSON_PATH, containsString("must match"))
                    }
                }
                it("unknown id") {
                    When {
                        get("/variable-definitions/${VariableDefinitionService.generateId()}")
                    } Then {
                        statusCode(HttpStatus.NOT_FOUND.code)
                        body(ERROR_MESSAGE_JSON_PATH, containsString("No such variable definition found"))
                    }
                }
            }
            describe("get variable definition at date") {
                withData(
                    nameFn = { (it.dateOfValidity?.format(DateTimeFormatter.ISO_LOCAL_DATE)).toString() },
                    DateTestCase(LocalDate.of(1800, 1, 1), HttpStatus.NOT_FOUND, null, null),
                    DateTestCase(null, HttpStatus.OK, LocalDate.of(2021, 1, 1), null),
                ) { (dateOfValidity, expectedStatus, expectedValidFrom, expectedValidUntil) ->
                    Given {
                        queryParam("date_of_validity", dateOfValidity?.format(DateTimeFormatter.ISO_LOCAL_DATE))
                    } When {
                        get("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}")
                    } Then {
                        statusCode(expectedStatus.code)
                        body("valid_from", equalTo(expectedValidFrom))
                        body("valid_until", equalTo(expectedValidUntil))
                    }
                }
            }
        },
    )
