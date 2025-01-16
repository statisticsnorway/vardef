package no.ssb.metadata.vardef.controllers.validityperiods

import io.micronaut.http.HttpStatus
import no.ssb.metadata.vardef.utils.SAVED_INTERNAL_VARIABLE_DEFINITION
import org.json.JSONObject
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.argumentSet
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
        fun checkValidUntilDates(): Stream<Arguments> =
            Stream.of(
                argumentSet(
                    "new validity period before closed validity period",
                    JSONObject()
                        .apply {
                            put("valid_from", "2022-05-21")
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
                    LocalDate.of(2022, 5, 21),
                    LocalDate.of(2023, 12, 31),
                ),
                argumentSet(
                    "new validity period after closed validity period",
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
                    "new validity period during closed validity period",
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
            )
    }
}
