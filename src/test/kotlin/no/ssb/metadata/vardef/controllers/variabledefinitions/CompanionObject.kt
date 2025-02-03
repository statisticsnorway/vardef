package no.ssb.metadata.vardef.controllers.variabledefinitions

import io.micronaut.http.HttpStatus
import no.ssb.metadata.vardef.utils.jsonTestInput
import org.json.JSONObject
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.argumentSet
import java.util.stream.Stream

class CompanionObject {
    companion object {
        @JvmStatic
        fun validUntilInCreateDraft(): Stream<Arguments> =
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
                    HttpStatus.BAD_REQUEST,
                ),
            )

        @JvmStatic
        fun createDraftMandatoryFields(): Stream<Arguments> =
            Stream.of(
                argumentSet(
                    "empty unit types",
                    jsonTestInput()
                        .apply {
                            put("unit_types", listOf(""))
                        }.toString(),
                    HttpStatus.BAD_REQUEST,
                ),
                argumentSet(
                    "empty subject fields",
                    jsonTestInput()
                        .apply {
                            put("subject_fields", listOf(""))
                        }.toString(),
                    HttpStatus.BAD_REQUEST,
                ),
                argumentSet(
                    "empty name",
                    jsonTestInput()
                        .apply {
                            put(
                                "name",
                                JSONObject()
                                    .apply {
                                        put("nb", "")
                                        put("nn", "")
                                        put("en", "")
                                    },
                            )
                        }.toString(),
                    HttpStatus.BAD_REQUEST,
                ),
                argumentSet(
                    "name one languages",
                    jsonTestInput()
                        .apply {
                            put(
                                "name",
                                JSONObject()
                                    .apply {
                                        put("nb", "halleluja")
                                        put("nn", "")
                                        put("en", "")
                                    },
                            )
                        }.toString(),
                    HttpStatus.CREATED,
                ),
                argumentSet(
                    "empty definition",
                    jsonTestInput()
                        .apply {
                            put(
                                "definition",
                                JSONObject()
                                    .apply {
                                        put("nb", "")
                                        put("nn", "")
                                        put("en", "")
                                    },
                            )
                        }.toString(),
                    HttpStatus.BAD_REQUEST,
                ),
                argumentSet(
                    "definition one language",
                    jsonTestInput()
                        .apply {
                            put(
                                "definition",
                                JSONObject()
                                    .apply {
                                        put("nb", "")
                                        put("nn", "")
                                        put("en", "oh my holy bike")
                                    },
                            )
                        }.toString(),
                    HttpStatus.CREATED,
                ),
            )
    }
}
