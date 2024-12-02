package no.ssb.metadata.vardef.controllers.patches

import no.ssb.metadata.vardef.models.SavedVariableDefinition
import no.ssb.metadata.vardef.utils.ALL_INCOME_TAX_PATCHES
import no.ssb.metadata.vardef.utils.SAVED_INTERNAL_VARIABLE_DEFINITION
import no.ssb.metadata.vardef.utils.TEST_DEVELOPERS_GROUP
import no.ssb.metadata.vardef.utils.jsonTestInput
import org.json.JSONObject
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.argumentSet
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
                    "Invalid Dapla group",
                ),
            )
    }
}
