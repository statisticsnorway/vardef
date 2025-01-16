package no.ssb.metadata.vardef.controllers.variabledefinitionbyid

import no.ssb.metadata.vardef.utils.DRAFT_EXAMPLE_WITH_VALID_UNTIL
import no.ssb.metadata.vardef.utils.SAVED_DRAFT_DEADWEIGHT_EXAMPLE
import org.json.JSONObject
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.argumentSet
import java.util.stream.Stream

class CompanionObject {
    companion object {
        @JvmStatic
        fun validOwnerUpdates(): Stream<Arguments> =
            Stream.of(
                argumentSet(
                    "New team name",
                    SAVED_DRAFT_DEADWEIGHT_EXAMPLE.owner.team,
                    JSONObject()
                        .apply {
                            put(
                                "owner",
                                JSONObject().apply {
                                    put("team", "my-oh-my-team")
                                    put(
                                        "groups",
                                        listOf(
                                            "skip-stat-developers",
                                            "play-enhjoern-a-developers",
                                            "my-oh-my-team-developers",
                                        ),
                                    )
                                },
                            )
                        }.toString(),
                    "owner.team",
                ),
                argumentSet(
                    "New group name",
                    SAVED_DRAFT_DEADWEIGHT_EXAMPLE.owner.groups[1],
                    JSONObject()
                        .apply {
                            put(
                                "owner",
                                JSONObject().apply {
                                    put("team", "skip-stat")
                                    put(
                                        "groups",
                                        listOf(
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
                    SAVED_DRAFT_DEADWEIGHT_EXAMPLE.owner.groups.last(),
                    JSONObject()
                        .apply {
                            put(
                                "owner",
                                JSONObject().apply {
                                    put("team", "skip-stat")
                                    put(
                                        "groups",
                                        listOf(
                                            "skip-stat-developers",
                                            "play-enhjoern-a-developers",
                                            "play-foeniks-a-developers",
                                        ),
                                    )
                                },
                            )
                        }.toString(),
                    "owner.groups[2]",
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
                    "owner team and groups can not be null",
                ),
                argumentSet(
                    "Groups list is null",
                    JSONObject()
                        .apply {
                            put(
                                "owner",
                                JSONObject().apply {
                                    put("team", "skip-stat")
                                },
                            )
                        }.toString(),
                    true,
                    "must not be empty",
                ),
                argumentSet(
                    "Groups empty values in list",
                    JSONObject()
                        .apply {
                            put(
                                "owner",
                                JSONObject().apply {
                                    put("team", "skip-stat")
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
                    "Invalid Dapla group",
                ),
                argumentSet(
                    "Invalid Team name",
                    JSONObject()
                        .apply {
                            put(
                                "owner",
                                JSONObject().apply {
                                    put("team", "playdida")
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
                    "Invalid Dapla team",
                ),
                argumentSet(
                    "Owner team is not coherent with groups",
                    JSONObject()
                        .apply {
                            put(
                                "owner",
                                JSONObject().apply {
                                    put("team", "skip-stat")
                                    put(
                                        "groups",
                                        listOf(
                                            "dapla-felles-developers",
                                        ),
                                    )
                                },
                            )
                        }.toString(),
                    false,
                    "group of the owning team must be included",
                ),
            )

        @JvmStatic
        fun inValidDateUpdates(): Stream<Arguments> =
            Stream.of(
                argumentSet(
                    "valid until before valid from",
                    JSONObject()
                        .apply {
                            put("valid_until", "1970-11-12")
                        }.toString(),
                    "Invalid date order.",
                ),
                argumentSet(
                    "valid from after valid until",
                    JSONObject()
                        .apply {
                            put("valid_from", "2041-02-22")
                        }.toString(),
                    "Invalid date order.",
                ),
            )

        @JvmStatic
        fun validDateUpdates(): Stream<Arguments> =
            Stream.of(
                argumentSet(
                    "valid until before previous valid until",
                    JSONObject()
                        .apply {
                            put("valid_until", "2024-11-12")
                        }.toString(),
                    "valid_until",
                    DRAFT_EXAMPLE_WITH_VALID_UNTIL.validUntil.toString(),
                ),
                argumentSet(
                    "valid until after previous valid until",
                    JSONObject()
                        .apply {
                            put("valid_until", "2041-02-22")
                        }.toString(),
                    "valid_until",
                    DRAFT_EXAMPLE_WITH_VALID_UNTIL.validUntil.toString(),
                ),
                argumentSet(
                    "valid from before valid until",
                    JSONObject()
                        .apply {
                            put("valid_from", "2012-10-29")
                        }.toString(),
                    "valid_from",
                    DRAFT_EXAMPLE_WITH_VALID_UNTIL.validFrom.toString(),
                ),
            )
    }
}
