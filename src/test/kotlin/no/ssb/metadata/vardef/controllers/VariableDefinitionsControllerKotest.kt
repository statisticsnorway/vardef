// package no.ssb.metadata.vardef.controllers
//
// import io.kotest.core.spec.style.DescribeSpec
// import io.kotest.datatest.withData
// import io.micronaut.http.HttpStatus
// import io.micronaut.runtime.server.EmbeddedServer
// import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
// import io.restassured.RestAssured
// import io.restassured.RestAssured.oauth2
// import io.restassured.filter.log.RequestLoggingFilter
// import io.restassured.filter.log.ResponseLoggingFilter
// import io.restassured.http.ContentType
// import io.restassured.module.kotlin.extensions.Given
// import io.restassured.module.kotlin.extensions.Then
// import io.restassured.module.kotlin.extensions.When
// import no.ssb.metadata.vardef.constants.ACTIVE_GROUP
// import no.ssb.metadata.vardef.repositories.VariableDefinitionRepository
// import no.ssb.metadata.vardef.services.PatchesService
// import no.ssb.metadata.vardef.services.VariableDefinitionService
// import no.ssb.metadata.vardef.utils.*
// import org.hamcrest.CoreMatchers.equalTo
// import org.hamcrest.Matchers.containsString
// import java.time.LocalDate
// import java.time.format.DateTimeFormatter
//
// data class DateTest(
//    val dateOfValidity: LocalDate?,
//    val expectedNumber: Int,
// )
//
// @MicronautTest
// class VariableDefinitionsControllerKotest(
//    private val embeddedServer: EmbeddedServer,
//    private val patches: PatchesService,
//    private val repository: VariableDefinitionRepository,
// ) : DescribeSpec(
//        {
//            beforeAny {
//                println("BeforeAny")
//                repository.deleteAll()
//                ALL_INCOME_TAX_PATCHES.forEach { patches.create(it) }
//                patches.create(DRAFT_BUS_EXAMPLE)
//                patches.create(SAVED_DRAFT_DEADWEIGHT_EXAMPLE)
//                patches.create(SAVED_DEPRECATED_VARIABLE_DEFINITION)
//                patches.create(SAVED_INTERNAL_VARIABLE_DEFINITION)
//                println("Patches: ${repository.findAll().map { "\n${it.definitionId} - ${it.patchId}" }}")
//            }
//            beforeSpec {
//                RestAssured.port = embeddedServer.port
//                RestAssured.replaceFiltersWith(RequestLoggingFilter(), ResponseLoggingFilter())
//                RestAssured.authentication = oauth2(JwtTokenHelper.jwtTokenSigned().parsedString)
//            }
//            describe("get variable definition") {
//                it("malformed id") {
//                    When {
//                        get("/variable-definitions/MALFORMED_ID")
//                    } Then {
//                        statusCode(HttpStatus.BAD_REQUEST.code)
//                        body(ERROR_MESSAGE_JSON_PATH, containsString("must match"))
//                    }
//                }
//                it("unknown id") {
//                    When {
//                        get("/variable-definitions/${VariableDefinitionService.generateId()}")
//                    } Then {
//                        statusCode(HttpStatus.NOT_FOUND.code)
//                        body(ERROR_MESSAGE_JSON_PATH, containsString("No such variable definition found"))
//                    }
//                }
//            }
//            describe("create variable definition returns complete response") {
//                val shortName = "blink"
//
//                val updatedJsonString =
//                    jsonTestInput()
//                        .apply {
//                            put("short_name", shortName)
//                        }.toString()
//
//                Given {
//                    contentType(ContentType.JSON)
//                    body(updatedJsonString).log()
//                    queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
//                } When {
//                    post("/variable-definitions")
//                } Then {
//                    statusCode(HttpStatus.CREATED.code)
//                    body("short_name", equalTo(shortName))
//                }
//            }
//            describe("filter variable definitions by date") {
//                withData(
//                    DateTest(LocalDate.of(1800, 1, 1), 0),
//                    DateTest(LocalDate.of(2021, 1, 1), 4),
//                    DateTest(LocalDate.of(2020, 1, 1), 1),
//                    DateTest(LocalDate.of(2024, 6, 5), 1),
//                    DateTest(LocalDate.of(3000, 12, 31), 4),
//                    DateTest(null, 5),
//                ) { (dateOfValidity, expectedNumber) ->
//
//                    Given {
//                        queryParam("date_of_validity", dateOfValidity?.format(DateTimeFormatter.ISO_LOCAL_DATE))
//                    } When {
//                        get("/variable-definitions")
//                    } Then {
//                        statusCode(200)
//                        body("size()", equalTo(expectedNumber))
//                    }
//                }
//            }
//        },
//    )
