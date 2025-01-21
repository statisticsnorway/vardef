package no.ssb.metadata.vardef.controllers.validityperiods

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
                    HTTP_CREATED,
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
                    HTTP_BAD_REQUEST,
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
                    HTTP_BAD_REQUEST,
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
                    HTTP_BAD_REQUEST,
                    null,
                    null,
                ),
                argumentSet(
                    "validity period before closed validity period",
                    JSONObject()
                        .apply {
                            put("valid_from", "2019-01-01")
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
                    HTTP_CREATED,
                    LocalDate.of(2019, 1, 1),
                    LocalDate.of(2019, 12, 31),
                ),
            )
    }
}
