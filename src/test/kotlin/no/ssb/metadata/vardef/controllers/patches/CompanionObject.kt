package no.ssb.metadata.vardef.controllers.patches

import no.ssb.metadata.vardef.models.SavedVariableDefinition
import no.ssb.metadata.vardef.utils.*
import org.json.JSONObject
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.argumentSet
import java.net.HttpURLConnection.*
import java.time.LocalDate
import java.util.stream.Stream

class CompanionObject {
    companion object {
        fun patchBody(): JSONObject =
            jsonTestInput()
                .apply {
                    remove("short_name")
                    remove("valid_from")
                }

        @JvmStatic
        fun patches(): List<SavedVariableDefinition> = ALL_INCOME_TAX_PATCHES

        @JvmStatic
        fun validOwnerUpdates(): Stream<Arguments> =
            Stream.of(
                argumentSet(
                    "New team name",
                    SAVED_INTERNAL_VARIABLE_DEFINITION.owner.team,
                    JSONObject()
                        .apply {
                            put(
                                "owner",
                                JSONObject().apply {
                                    put("team", "my-oh-my-team")
                                    put(
                                        "groups",
                                        listOf(
                                            "my-oh-my-team-developers",
                                            "skip-stat-developers",
                                            "play-enhjoern-a-developers",
                                        ),
                                    )
                                },
                            )
                        }.toString(),
                    "owner.team",
                ),
                argumentSet(
                    "New group name",
                    SAVED_INTERNAL_VARIABLE_DEFINITION.owner.groups[1],
                    JSONObject()
                        .apply {
                            put(
                                "owner",
                                JSONObject().apply {
                                    put("team", "my-team")
                                    put(
                                        "groups",
                                        listOf(
                                            "my-team-developers",
                                            "skip-stat-developers",
                                            "play-foeniks-a-developers",
                                        ),
                                    )
                                },
                            )
                        }.toString(),
                    "owner.groups[1]",
                ),
                argumentSet(
                    "Add group name",
                    SAVED_INTERNAL_VARIABLE_DEFINITION.owner.groups.last(),
                    JSONObject()
                        .apply {
                            put(
                                "owner",
                                JSONObject().apply {
                                    put("team", "my-team")
                                    put(
                                        "groups",
                                        listOf(
                                            "my-team-developers",
                                            "skip-stat-developers",
                                            "play-enhjoern-a-developers",
                                            "play-foeniks-a-developers",
                                        ),
                                    )
                                },
                            )
                        }.toString(),
                    "owner.groups[1]",
                ),
            )

        @JvmStatic
        fun invalidOwnerUpdates(): Stream<Arguments> =
            Stream.of(
                argumentSet(
                    "Team name empty string",
                    JSONObject()
                        .apply {
                            put(
                                "owner",
                                JSONObject().apply {
                                    put("team", "")
                                    put(
                                        "groups",
                                        listOf(
                                            "skip-stat-developers",
                                            "play-enhjoern-a-developers",
                                        ),
                                    )
                                },
                            )
                        }.toString(),
                    true,
                    "owner.team",
                    "Invalid Dapla team",
                ),
                argumentSet(
                    "Team name null",
                    JSONObject()
                        .apply {
                            put(
                                "owner",
                                JSONObject().apply {
                                    put(
                                        "groups",
                                        listOf(
                                            "skip-stat-developers",
                                            "play-enhjoern-a-developers",
                                        ),
                                    )
                                },
                            )
                        }.toString(),
                    false,
                    null,
                    "owner team and groups can not be null",
                ),
                argumentSet(
                    "Groups list is null",
                    JSONObject()
                        .apply {
                            put(
                                "owner",
                                JSONObject().apply {
                                    put("team", "my-team")
                                },
                            )
                        }.toString(),
                    true,
                    "owner.groups",
                    "must not be empty",
                ),
                argumentSet(
                    "Groups values are null",
                    JSONObject()
                        .apply {
                            put(
                                "owner",
                                JSONObject().apply {
                                    put("team", "my-team")
                                    put("groups", listOf(null))
                                },
                            )
                        }.toString(),
                    true,
                    "owner.groups",
                    "must not be empty",
                ),
                argumentSet(
                    "Groups empty values in list",
                    JSONObject()
                        .apply {
                            put(
                                "owner",
                                JSONObject().apply {
                                    put("team", "my-team")
                                    put(
                                        "groups",
                                        listOf(
                                            "",
                                            "",
                                        ),
                                    )
                                },
                            )
                        }.toString(),
                    true,
                    "owner.groups",
                    "Invalid Dapla group",
                ),
                argumentSet(
                    "Remove owner team developers group",
                    JSONObject()
                        .apply {
                            put(
                                "owner",
                                JSONObject().apply {
                                    put("team", "my-team")
                                    put(
                                        "groups",
                                        listOf(
                                            "other-group",
                                            TEST_DEVELOPERS_GROUP,
                                        ),
                                    )
                                },
                            )
                        }.toString(),
                    true,
                    "owner.groups",
                    "Invalid Dapla group",
                ),
                argumentSet(
                    "Change owner team without changing developers group",
                    JSONObject()
                        .apply {
                            put(
                                "owner",
                                JSONObject().apply {
                                    put("team", "other-team")
                                    put(
                                        "groups",
                                        listOf(
                                            "my-team-developers",
                                            "other-group",
                                            TEST_DEVELOPERS_GROUP,
                                        ),
                                    )
                                },
                            )
                        }.toString(),
                    true,
                    "owner.groups",
                    "Invalid Dapla group",
                ),
                argumentSet(
                    "Invalid group name",
                    JSONObject()
                        .apply {
                            put(
                                "owner",
                                JSONObject().apply {
                                    put("team", "my-team")
                                    put(
                                        "groups",
                                        listOf(
                                            "my-team-developers",
                                            "pipi-managers",
                                            TEST_DEVELOPERS_GROUP,
                                        ),
                                    )
                                },
                            )
                        }.toString(),
                    true,
                    "owner.groups",
                    "Invalid Dapla group",
                ),
            )

        @JvmStatic
        fun patchValidUntil(): Stream<Arguments> =
            Stream.of(
                argumentSet(
                    "can only patch latest",
                    patchBody().apply { put("valid_until", "2030-06-30") }.toString(),
                    INCOME_TAX_VP1_P1.definitionId,
                    "1980-01-01",
                    HTTP_BAD_REQUEST,
                ),
                argumentSet(
                    "can not patch valid until before valid from",
                    patchBody().apply { put("valid_until", "2020-12-31") }.toString(),
                    INCOME_TAX_VP1_P1.definitionId,
                    null,
                    HTTP_BAD_REQUEST,
                ),
                argumentSet(
                    "validity period is closed",
                    patchBody().apply { put("valid_until", "2030-06-30") }.toString(),
                    SAVED_INTERNAL_VARIABLE_DEFINITION.definitionId,
                    null,
                    HTTP_BAD_REQUEST,
                ),
                argumentSet(
                    "validity period is open",
                    patchBody().apply { put("valid_until", "2030-06-30") }.toString(),
                    INCOME_TAX_VP1_P1.definitionId,
                    null,
                    HTTP_CREATED,
                ),
                argumentSet(
                    "validity period is open valid until is before valid_from",
                    patchBody().apply { put("valid_until", "2019-06-30") }.toString(),
                    SAVED_INTERNAL_VARIABLE_DEFINITION_NO_VALID_UNTIL.definitionId,
                    null,
                    HTTP_BAD_REQUEST,
                ),
                argumentSet(
                    "not latest validity period",
                    patchBody().apply { put("valid_until", "2030-06-30") }.toString(),
                    INCOME_TAX_VP1_P1.definitionId,
                    "1980-01-01",
                    HTTP_BAD_REQUEST,
                ),
                argumentSet(
                    "validity period does not exist - select specific period",
                    patchBody().apply { put("valid_until", "2020-06-30") }.toString(),
                    SAVED_INTERNAL_VARIABLE_DEFINITION.definitionId,
                    "1980-01-01",
                    HTTP_NOT_FOUND,
                ),
            )
    }
}
