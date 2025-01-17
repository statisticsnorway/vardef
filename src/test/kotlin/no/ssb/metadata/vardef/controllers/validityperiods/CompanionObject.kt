package no.ssb.metadata.vardef.controllers.validityperiods

import io.micronaut.http.HttpStatus
import no.ssb.metadata.vardef.utils.SAVED_INTERNAL_VARIABLE_DEFINITION
import org.json.JSONObject
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.argumentSet
import java.net.HttpURLConnection.HTTP_BAD_REQUEST
import java.net.HttpURLConnection.HTTP_CREATED
import java.time.LocalDate
import java.util.stream.Stream

class CompanionObject {
    companion object {
        @JvmStatic
        fun allMandatoryFieldsChanged(): String =
            JSONObject()
                .apply {
                    put("valid_from", "2024-01-11")
                    put(
                        "definition",
                        JSONObject().apply {
                            put("nb", "Intektsskatt atter ny definisjon")
                            put("nn", "Intektsskatt atter ny definisjon")
                            put("en", "Yet another definition")
                        },
                    )
                }.toString()

        @JvmStatic
        fun noneMandatoryFieldsChanged(): String {
            val testCase =
                JSONObject()
                    .apply {
                        put("valid_from", "2021-01-01")
                        put(
                            "definition",
                            JSONObject().apply {
                                put("nb", "Intektsskatt ny definisjon")
                                put("nn", "Intektsskatt ny definisjon")
                                put("en", "Income tax new definition")
                            },
                        )
                    }.toString()
            return testCase
        }

        @JvmStatic
        fun newValidityPeriods(): Stream<Arguments> =
            Stream.of(
                argumentSet(
                    "validity period after closed validity period",
                    JSONObject()
                        .apply {
                            put("valid_from", "2031-01-11")
                            put(
                                "definition",
                                JSONObject().apply {
                                    put("nb", "Intektsskatt atter ny definisjon")
                                    put("nn", "Intektsskatt atter ny definisjon")
                                    put("en", "Yet another definition")
                                },
                            )
                        }.toString(),
                    SAVED_INTERNAL_VARIABLE_DEFINITION.definitionId,
                    HttpStatus.CREATED,
                    LocalDate.of(2031, 1, 11),
                    null,
                ),
                argumentSet(
                    "validity period during closed validity period",
                    JSONObject()
                        .apply {
                            put("valid_from", "2026-01-11")
                            put(
                                "definition",
                                JSONObject().apply {
                                    put("nb", "Intektsskatt atter ny definisjon")
                                    put("nn", "Intektsskatt atter ny definisjon")
                                    put("en", "Yet another definition")
                                },
                            )
                        }.toString(),
                    SAVED_INTERNAL_VARIABLE_DEFINITION.definitionId,
                    HttpStatus.BAD_REQUEST,
                    null,
                    null,
                ),
                argumentSet(
                    "validity period same date closed validity period ended",
                    JSONObject()
                        .apply {
                            put("valid_from", "2030-01-01")
                            put(
                                "definition",
                                JSONObject().apply {
                                    put("nb", "Intektsskatt atter ny definisjon")
                                    put("nn", "Intektsskatt atter ny definisjon")
                                    put("en", "Yet another definition")
                                },
                            )
                        }.toString(),
                    SAVED_INTERNAL_VARIABLE_DEFINITION.definitionId,
                    HttpStatus.BAD_REQUEST,
                    null,
                    null,
                ),
                argumentSet(
                    "validity period same date closed validity period started",
                    JSONObject()
                        .apply {
                            put("valid_from", "2024-01-01")
                            put(
                                "definition",
                                JSONObject().apply {
                                    put("nb", "Intektsskatt atter ny definisjon")
                                    put("nn", "Intektsskatt atter ny definisjon")
                                    put("en", "Yet another definition")
                                },
                            )
                        }.toString(),
                    SAVED_INTERNAL_VARIABLE_DEFINITION.definitionId,
                    HttpStatus.BAD_REQUEST,
                    null,
                    null,
                ),
            )

        @JvmStatic
        fun testOnlyClosedValidityPeriods(): Stream<Arguments> =
            Stream.of(
            argumentSet(
                "valid from before all",
                JSONObject()
                    .apply {
                        put("valid_from", "1969-01-01")
                        put(
                            "definition",
                            JSONObject().apply {
                                put("nb", "Intektsskatt atter ny definisjon")
                                put("nn", "Intektsskatt atter ny definisjon")
                                put("en", "Yet another definition")
                            },
                        )
                    }.toString(),
                HTTP_CREATED,
            ),
                argumentSet(
                    "valid from after all",
                    JSONObject()
                        .apply {
                            put("valid_from", "2030-01-02")
                            put(
                                "definition",
                                JSONObject().apply {
                                    put("nb", "Intektsskatt atter ny definisjon")
                                    put("nn", "Intektsskatt atter ny definisjon")
                                    put("en", "Yet another definition")
                                },
                            )
                        }.toString(),
                    HTTP_CREATED,
                ),
                argumentSet(
                    "valid from same day valid until last period",
                    JSONObject()
                        .apply {
                            put("valid_from", "2030-01-01")
                            put(
                                "definition",
                                JSONObject().apply {
                                    put("nb", "Intektsskatt atter ny definisjon")
                                    put("nn", "Intektsskatt atter ny definisjon")
                                    put("en", "Yet another definition")
                                },
                            )
                        }.toString(),
                    HTTP_BAD_REQUEST,
                ),
                argumentSet(
                    "valid from same day valid until second period",
                    JSONObject()
                        .apply {
                            put("valid_from", "2023-12-31")
                            put(
                                "definition",
                                JSONObject().apply {
                                    put("nb", "Intektsskatt atter ny definisjon")
                                    put("nn", "Intektsskatt atter ny definisjon")
                                    put("en", "Yet another definition")
                                },
                            )
                        }.toString(),
                    HTTP_BAD_REQUEST,
                ),
                argumentSet(
                    "valid from same day valid until first period",
                    JSONObject()
                        .apply {
                            put("valid_from", "1989-12-31")
                            put(
                                "definition",
                                JSONObject().apply {
                                    put("nb", "Intektsskatt atter ny definisjon")
                                    put("nn", "Intektsskatt atter ny definisjon")
                                    put("en", "Yet another definition")
                                },
                            )
                        }.toString(),
                    HTTP_BAD_REQUEST,
                ),
                argumentSet(
                    "valid from during last validity period",
                    JSONObject()
                        .apply {
                            put("valid_from", "2027-09-11")
                            put(
                                "definition",
                                JSONObject().apply {
                                    put("nb", "Intektsskatt atter ny definisjon")
                                    put("nn", "Intektsskatt atter ny definisjon")
                                    put("en", "Yet another definition")
                                },
                            )
                        }.toString(),
                    HTTP_BAD_REQUEST,
                ),
                argumentSet(
                    "valid from during second validity period",
                    JSONObject()
                        .apply {
                            put("valid_from", "2021-03-21")
                            put(
                                "definition",
                                JSONObject().apply {
                                    put("nb", "Intektsskatt atter ny definisjon")
                                    put("nn", "Intektsskatt atter ny definisjon")
                                    put("en", "Yet another definition")
                                },
                            )
                        }.toString(),
                    HTTP_BAD_REQUEST,
                ),
                argumentSet(
                    "valid from during first validity period",
                    JSONObject()
                        .apply {
                            put("valid_from", "1972-03-21")
                            put(
                                "definition",
                                JSONObject().apply {
                                    put("nb", "Intektsskatt atter ny definisjon")
                                    put("nn", "Intektsskatt atter ny definisjon")
                                    put("en", "Yet another definition")
                                },
                            )
                        }.toString(),
                    HTTP_BAD_REQUEST,
                ),
                argumentSet(
                    "valid from in gap between periods",
                    JSONObject()
                        .apply {
                            put("valid_from", "2019-12-31")
                            put(
                                "definition",
                                JSONObject().apply {
                                    put("nb", "Intektsskatt atter ny definisjon")
                                    put("nn", "Intektsskatt atter ny definisjon")
                                    put("en", "Yet another definition")
                                },
                            )
                        }.toString(),
                    HTTP_CREATED,
                ),
            )

    }
}
