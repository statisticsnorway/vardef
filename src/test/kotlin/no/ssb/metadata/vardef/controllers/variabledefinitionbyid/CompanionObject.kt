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
                ),
                argumentSet(
                    "valid from after valid until",
                    JSONObject()
                        .apply {
                            put("valid_from", "2041-02-22")
                        }.toString(),
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

        @JvmStatic
        fun updateMandatoryFields(): Stream<Arguments> =
            Stream.of(
                argumentSet(
                    "empty unit types list",
                    JSONObject().apply {
                        put("unit_types", listOf(null))
                    }.toString(),
                    "must not be empty",
                ),
                argumentSet(
                    "blank values in unit types list",
                    JSONObject().apply {
                        put("unit_types", listOf(""))
                    }.toString(),
                    "Code  is not a member of classification with id 702",
                ),
                argumentSet(
                    "blank values in subject fields list",
                    JSONObject().apply {
                        put("subject_fields", listOf("", " "))
                    }.toString(),
                    "Code  is not a member of classification with id 618",
                ),
                argumentSet(
                    "empty subject fields list",
                    JSONObject().apply {
                        put("subject_fields", listOf(null))
                    }.toString(),
                    "must not be empty",
                ),
                argumentSet(
                    "imvalid contact",
                    JSONObject().apply {
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
                    "Must have value for at least one language",
                ),
                argumentSet(
                    "contact invalid email",
                    JSONObject().apply {
                        put(
                            "contact",
                            JSONObject().apply {
                                put(
                                    "title",
                                    JSONObject().apply {
                                        put("nb", "Seksjon High end")
                                    },
                                )
                                put("email", "chgjcgh")
                            },
                        )
                    }.toString(),
                    "must be a well-formed email address",
                ),
                argumentSet(
                    "contact missing email",
                    JSONObject().apply {
                        put(
                            "contact",
                            JSONObject().apply {
                                put(
                                    "title",
                                    JSONObject().apply {
                                        put("nb", "Seksjon High end")
                                    },
                                )
                                put("email", "")
                            },
                        )
                    }.toString(),
                    "must be a well-formed email address",
                ),
                argumentSet(
                    "blank short name",
                    JSONObject().apply {
                        put("short_name", "")
                    }.toString(),
                    "must match \"^[a-z0-9_]{2,}\$\"",
                ),
                argumentSet(
                    "empty values all languages name",
                    JSONObject().apply {
                        put(
                            "name",
                            JSONObject().apply {
                                put("nb", "")
                                put("nn", "")
                                put("en", " ")
                            },
                        )
                    }.toString(),
                    "Must have value for at least one language",
                ),
            )
    }
}
