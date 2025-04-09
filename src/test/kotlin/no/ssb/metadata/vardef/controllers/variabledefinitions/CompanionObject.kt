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
        fun createLanguageStringType(): Stream<Arguments> =
            Stream.of(
                argumentSet(
                    "Contact",
                    jsonTestInput()
                        .apply {
                            put(
                                "contact",
                                JSONObject().apply {
                                    put(
                                        "title",
                                        JSONObject().apply {
                                            put("nb", "  spion  ")
                                            put("nn", "")
                                            put("en", "")
                                        },
                                    )
                                    put("email", "spy@ssb.no")
                                },
                            )
                        }.toString(),
                    "contact.title.nb",
                    "spion",
                ),
                argumentSet(
                    "Definition",
                    jsonTestInput()
                        .apply {
                            put(
                                "definition",
                                JSONObject().apply {
                                    put(
                                        "nb",
                                        "",
                                    )
                                    put(
                                        "nn",
                                        "",
                                    )
                                    put(
                                        "en",
                                        "Oh my unicorn   ",
                                    )
                                },
                            )
                        }.toString(),
                    "definition.en",
                    "Oh my unicorn",
                ),
                argumentSet(
                    "Name",
                    jsonTestInput()
                        .apply {
                            put(
                                "name",
                                JSONObject().apply {
                                    put(
                                        "nb",
                                        "",
                                    )
                                    put(
                                        "nn",
                                        "superlongs ",
                                    )
                                    put(
                                        "en",
                                        "",
                                    )
                                },
                            )
                        }.toString(),
                    "name.nn",
                    "superlongs",
                ),
            )

        @JvmStatic
        fun createDraftMandatoryFields(): Stream<Arguments> =
            Stream.of(
                argumentSet(
                    "blank values contact",
                    jsonTestInput()
                        .apply {
                            put(
                                "contact",
                                JSONObject().apply {
                                    put(
                                        "title",
                                        JSONObject().apply {
                                            put("nb", "")
                                            put("nn", "")
                                            put("en", "")
                                        },
                                    )
                                    put("email", "")
                                },
                            )
                        }.toString(),
                    HttpStatus.BAD_REQUEST,
                ),
                argumentSet(
                    "contact title null",
                    jsonTestInput()
                        .apply {
                            put(
                                "contact",
                                JSONObject().apply {
                                    put(
                                        "title",
                                        JSONObject().apply {
                                        },
                                    )
                                    put("email", "contact@test.com")
                                },
                            )
                        }.toString(),
                    HttpStatus.BAD_REQUEST,
                ),
                argumentSet(
                    "blank value contact email",
                    jsonTestInput()
                        .apply {
                            put(
                                "contact",
                                JSONObject().apply {
                                    put(
                                        "title",
                                        JSONObject().apply {
                                            put("nb", "Seksjon helse")
                                            put("en", "Section for health")
                                        },
                                    )
                                    put("email", "")
                                },
                            )
                        }.toString(),
                    HttpStatus.BAD_REQUEST,
                ),
                argumentSet(
                    "invalid email",
                    jsonTestInput()
                        .apply {
                            put(
                                "contact",
                                JSONObject().apply {
                                    put(
                                        "title",
                                        JSONObject().apply {
                                            put("nb", "Seksjon helse")
                                            put("en", "Section for health")
                                        },
                                    )
                                    put("email", "@some.far")
                                },
                            )
                        }.toString(),
                    HttpStatus.BAD_REQUEST,
                ),
                argumentSet(
                    "contact",
                    jsonTestInput()
                        .apply {
                            put(
                                "contact",
                                JSONObject().apply {
                                    put(
                                        "title",
                                        JSONObject().apply {
                                            put("nb", "Seksjon sykler")
                                        },
                                    )
                                    put("email", "section@test.com")
                                },
                            )
                        }.toString(),
                    HttpStatus.CREATED,
                ),
                argumentSet(
                    "blank values unit types",
                    jsonTestInput()
                        .apply {
                            put("unit_types", listOf(""))
                        }.toString(),
                    HttpStatus.BAD_REQUEST,
                ),
                argumentSet(
                    "empty unit types",
                    jsonTestInput()
                        .apply {
                            put("unit_types", listOf(null))
                        }.toString(),
                    HttpStatus.BAD_REQUEST,
                ),
                argumentSet(
                    "blank values subject fields",
                    jsonTestInput()
                        .apply {
                            put("subject_fields", listOf(""))
                        }.toString(),
                    HttpStatus.BAD_REQUEST,
                ),
                argumentSet(
                    "empty subject fields",
                    jsonTestInput()
                        .apply {
                            put("subject_fields", listOf(null))
                        }.toString(),
                    HttpStatus.BAD_REQUEST,
                ),
                argumentSet(
                    "blank value shortname",
                    jsonTestInput()
                        .apply {
                            put("short_name", "")
                        }.toString(),
                    HttpStatus.BAD_REQUEST,
                ),
                argumentSet(
                    "blank value valid from",
                    jsonTestInput()
                        .apply {
                            put("valid_from", "")
                        }.toString(),
                    HttpStatus.BAD_REQUEST,
                ),
                argumentSet(
                    "null value valid from",
                    """{"valid_from": null}""".trimIndent(),
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
