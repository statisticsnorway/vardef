package no.ssb.metadata.vardef.controllers

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.spec.style.DescribeSpec
import io.micronaut.http.HttpStatus
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.extensions.kotest5.MicronautKotest5Extension
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import io.restassured.RestAssured
import io.restassured.RestAssured.oauth2
import io.restassured.filter.log.RequestLoggingFilter
import io.restassured.filter.log.ResponseLoggingFilter
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import no.ssb.metadata.vardef.utils.ERROR_MESSAGE_JSON_PATH
import no.ssb.metadata.vardef.utils.JwtTokenHelper
import org.hamcrest.Matchers.containsString

@Suppress("unused")
object ProjectConfig : AbstractProjectConfig() {
    override fun extensions() = listOf(MicronautKotest5Extension)
}

@MicronautTest
class VariableDefinitionByIdControllerKotest(
    private val embeddedServer: EmbeddedServer,
) : DescribeSpec(
        {
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
            }
        },
    )
