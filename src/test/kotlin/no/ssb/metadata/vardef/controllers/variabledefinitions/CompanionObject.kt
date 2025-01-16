package no.ssb.metadata.vardef.controllers.variabledefinitions

import io.micronaut.http.HttpStatus
import no.ssb.metadata.vardef.utils.jsonTestInput
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.argumentSet
import java.util.stream.Stream

class CompanionObject {
    companion object {
        @JvmStatic
        fun validUntilInRequest(): Stream<Arguments> =
            Stream.of(
                argumentSet(
                    "valid until before valid from",
                    jsonTestInput()
                        .apply {
                            put("valid_until", "2020-01-06")
                        }.toString(),
                    HttpStatus.BAD_REQUEST,
                ),
                argumentSet(
                    "valid until after valid from",
                    jsonTestInput()
                        .apply {
                            put("valid_until", "2040-11-10")
                        }.toString(),
                    HttpStatus.CREATED,
                ),
                argumentSet(
                    "valid until equal valid from",
                    jsonTestInput()
                        .apply {
                            put("valid_until", "2024-06-05")
                        }.toString(),
                    HttpStatus.CREATED,
                ),
            )
    }
}
