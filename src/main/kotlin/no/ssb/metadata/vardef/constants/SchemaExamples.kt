package no.ssb.metadata.vardef.constants

const val ID_EXAMPLE = """wypvb3wd"""
const val ID_INVALID_EXAMPLE = "invalid id"
const val EMPTY_LIST_EXAMPLE = """[]"""
const val DATE_EXAMPLE = "1970-01-01"
const val ACTIVE_GROUP_EXAMPLE = "dapla-felles-developers"
const val SHORT_NAME_EXAMPLE = "landbak"

// The following should be used to ensure that example names match
const val NOT_FOUND_EXAMPLE_NAME = "Not found"
const val METHOD_NOT_ALLOWED_EXAMPLE_NAME = "Method not allowed"
const val CONFLICT_EXAMPLE_NAME = "Conflict"
const val BAD_REQUEST_EXAMPLE_NAME = "Bad request"
const val CONSTRAINT_VIOLATION_EXAMPLE_NAME = "Constraint violation"

const val KLASS_REFERENCE_SUBJECT_FIELD_EXAMPLE = """{
            "reference_uri": "https://www.ssb.no/klass/klassifikasjoner/$SUBJECT_FIELDS_KLASS_CODE",
            "code": "be07",
            "title": "Innvandrere"
        }"""

const val RENDERED_CONTACT_EXAMPLE = """{
        "title": "Seksjon for befolkningsstatistikk",
        "email": "s320@ssb.no"
    }"""

const val OWNER_EXAMPLE = """{
    "team": "play-obr-b",
    "groups": ["play-obr-b-developers"]
}"""

const val DRAFT_EXAMPLE = """{
    "name": {
        "en": "Country Background",
        "nb": "Landbakgrunn",
        "nn": "Landbakgrunn"
    },
    "short_name": "landbak",
    "definition": {
        "en": "Country background is the person's own, the mother's or possibly the father's country of birth. Persons without an immigrant background always have Norway as country background. In cases where the parents have different countries of birth the mother's country of birth is chosen. If neither the person nor the parents are born abroad, country background is chosen from the first person born abroad in the order mother's mother, mother's father, father's mother, father's father.",
        "nb": "For personer født i utlandet, er dette (med noen få unntak) eget fødeland. For personer født i Norge er det foreldrenes fødeland. I de tilfeller der foreldrene har ulikt fødeland, er det morens fødeland som blir valgt. Hvis ikke personen selv eller noen av foreldrene er utenlandsfødt, hentes landbakgrunn fra de første utenlandsfødte en treffer på i rekkefølgen mormor, morfar, farmor eller farfar.",
        "nn": "For personar fødd i utlandet, er dette (med nokre få unntak) eige fødeland. For personar fødd i Noreg er det fødelandet til foreldra. I dei tilfella der foreldra har ulikt fødeland, er det fødelandet til mora som blir valt. Viss ikkje personen sjølv eller nokon av foreldra er utenlandsfødt, blir henta landsbakgrunn frå dei første utenlandsfødte ein treffar på i rekkjefølgja mormor, morfar, farmor eller farfar."
    },
    "classification_reference": "91",
    "unit_types": ["01", "02"],
    "subject_fields": ["he04"],
    "contains_special_categories_of_personal_data": true,
    "measurement_type": null,
    "valid_from": "2003-01-01",
    "external_reference_uri": "https://www.ssb.no/a/metadata/conceptvariable/vardok/1919/nb",
    "comment": {
        "nb": "Fra og med 1.1.2003 ble definisjon endret til også å trekke inn besteforeldrenes fødeland.",
        "nn": "Fra og med 1.1.2003 ble definisjon endret til også å trekke inn besteforeldrenes fødeland.",
        "en": "As of 1 January 2003, the definition was changed to also include the grandparents' country of birth."
    },
    "related_variable_definition_uris": [
        "https://example.com/"
    ],
    "contact": {
        "title": {
            "en": "Division for population statistics",
            "nb": "Seksjon for befolkningsstatistikk",
            "nn": "Seksjon for befolkningsstatistikk"
        },
        "email": "s320@ssb.no"
    }
}"""

const val UPDATE_DRAFT_EXAMPLE = """{"classification_reference": $UNIT_TYPES_KLASS_CODE}"""

const val UPDATE_DRAFT_CONSTRAINT_VIOLATION_EXAMPLE = """{"classification_reference": "incorrect"}"""

const val COMPLETE_RESPONSE_EXAMPLE = """{
    "id": "$ID_EXAMPLE",
    "patch_id": 1,
    "name": {
        "en": "Country Background",
        "nb": "Landbakgrunn",
        "nn": "Landbakgrunn"
    },
    "short_name": "landbak",
    "definition": {
        "en": "Country background is the person's own, the mother's or possibly the father's country of birth. Persons without an immigrant background always have Norway as country background. In cases where the parents have different countries of birth the mother's country of birth is chosen. If neither the person nor the parents are born abroad, country background is chosen from the first person born abroad in the order mother's mother, mother's father, father's mother, father's father.",
        "nb": "For personer født i utlandet, er dette (med noen få unntak) eget fødeland. For personer født i Norge er det foreldrenes fødeland. I de tilfeller der foreldrene har ulikt fødeland, er det morens fødeland som blir valgt. Hvis ikke personen selv eller noen av foreldrene er utenlandsfødt, hentes landbakgrunn fra de første utenlandsfødte en treffer på i rekkefølgen mormor, morfar, farmor eller farfar.",
        "nn": "For personar fødd i utlandet, er dette (med nokre få unntak) eige fødeland. For personar fødd i Noreg er det fødelandet til foreldra. I dei tilfella der foreldra har ulikt fødeland, er det fødelandet til mora som blir valt. Viss ikkje personen sjølv eller nokon av foreldra er utenlandsfødt, blir henta landsbakgrunn frå dei første utenlandsfødte ein treffar på i rekkjefølgja mormor, morfar, farmor eller farfar."
    },
    "classification_reference": "91",
    "unit_types": ["01", "02"],
    "subject_fields": ["he04"],
    "contains_special_categories_of_personal_data": true,
    "variable_status": "DRAFT",
    "measurement_type": null,
    "valid_from": "2003-01-01",
    "valid_until": null,
    "external_reference_uri": "https://www.ssb.no/a/metadata/conceptvariable/vardok/1919/nb",
    "comment": {
        "nb": "Fra og med 1.1.2003 ble definisjon endret til også å trekke inn besteforeldrenes fødeland.",
        "nn": "Fra og med 1.1.2003 ble definisjon endret til også å trekke inn besteforeldrenes fødeland.",
        "en": "As of 1 January 2003, the definition was changed to also include the grandparents' country of birth."
    },
    "related_variable_definition_uris": [
        "https://example.com/"
    ],
    "owner": {
        "team": "team-a",
        "groups": ["team-a-developers"]
    },
    "contact": {
        "title": {
            "en": "Division for population statistics",
            "nb": "Seksjon for befolkningsstatistikk",
            "nn": "Seksjon for befolkningsstatistikk"
        },
        "email": "s320@ssb.no"
    },
    "created_at": "2024-06-11T08:15:19.038Z",
    "created_by": "ano@ssb.no",
    "last_updated_at": "2024-06-11T08:15:19.038Z",
    "last_updated_by": "ano@ssb.no"
}
"""

const val COMPLETE_RESPONSE_EXAMPLE_PUBLISHED_VARIABLE = """{
    "id": "$ID_EXAMPLE",
    "patch_id": 2,
    "name": {
        "en": "Country Background",
        "nb": "Landbakgrunn",
        "nn": "Landbakgrunn"
    },
    "short_name": "landbak",
    "definition": {
        "en": "Country background is the person's own, the mother's or possibly the father's country of birth. Persons without an immigrant background always have Norway as country background. In cases where the parents have different countries of birth the mother's country of birth is chosen. If neither the person nor the parents are born abroad, country background is chosen from the first person born abroad in the order mother's mother, mother's father, father's mother, father's father.",
        "nb": "For personer født i utlandet, er dette (med noen få unntak) eget fødeland. For personer født i Norge er det foreldrenes fødeland. I de tilfeller der foreldrene har ulikt fødeland, er det morens fødeland som blir valgt. Hvis ikke personen selv eller noen av foreldrene er utenlandsfødt, hentes landbakgrunn fra de første utenlandsfødte en treffer på i rekkefølgen mormor, morfar, farmor eller farfar.",
        "nn": "For personar fødd i utlandet, er dette (med nokre få unntak) eige fødeland. For personar fødd i Noreg er det fødelandet til foreldra. I dei tilfella der foreldra har ulikt fødeland, er det fødelandet til mora som blir valt. Viss ikkje personen sjølv eller nokon av foreldra er utenlandsfødt, blir henta landsbakgrunn frå dei første utenlandsfødte ein treffar på i rekkjefølgja mormor, morfar, farmor eller farfar."
    },
    "classification_reference": "91",
    "unit_types": ["01", "02"],
    "subject_fields": ["he04"],
    "contains_special_categories_of_personal_data": true,
    "variable_status": "PUBLISHED_INTERNAL",
    "measurement_type": null,
    "valid_from": "2003-01-01",
    "valid_until": null,
    "external_reference_uri": "https://www.ssb.no/a/metadata/conceptvariable/vardok/1919/nb",
    "comment": {
        "nb": "Fra og med 1.1.2003 ble definisjon endret til også å trekke inn besteforeldrenes fødeland.",
        "nn": "Fra og med 1.1.2003 ble definisjon endret til også å trekke inn besteforeldrenes fødeland.",
        "en": "As of 1 January 2003, the definition was changed to also include the grandparents' country of birth."
    },
    "related_variable_definition_uris": [
        "https://example.com/"
    ],
    "owner": {
        "team": "team-a",
        "groups": ["team-a-developers"]
    },
    "contact": {
        "title": {
            "en": "Division for population statistics",
            "nb": "Seksjon for befolkningsstatistikk",
            "nn": "Seksjon for befolkningsstatistikk"
        },
        "email": "s320@ssb.no"
    },
    "created_at": "2024-06-11T08:15:19.038Z",
    "created_by": "ano@ssb.no",
    "last_updated_at": "2024-06-11T08:15:19.038Z",
    "last_updated_by": "ano@ssb.no"
}
"""

const val PATCH_EXAMPLE = """
{
    "name": {
        "en": "Country Background",
        "nb": "Landbakgrunnen",
        "nn": "Landbakgrunnen"
    },
    "definition": {
        "en": "Country background is the person's own, the mother's or possibly the father's country of birth. Persons without an immigrant background always have Norway as country background. In cases where the parents have different countries of birth the mother's country of birth is chosen. If neither the person nor the parents are born abroad, country background is chosen from the first person born abroad in the order mother's mother, mother's father, father's mother, father's father.",
        "nb": "For personer født i utlandet, er dette (med noen få unntak) eget fødeland. For personer født i Norge er det foreldrenes fødeland. I de tilfeller der foreldrene har ulikt fødeland, er det morens fødeland som blir valgt. Hvis ikke personen selv eller noen av foreldrene er utenlandsfødt, hentes landbakgrunn fra de første utenlandsfødte en treffer på i rekkefølgen mormor, morfar, farmor eller farfar.",
        "nn": "For personar fødd i utlandet, er dette (med nokre få unntak) eige fødeland. For personar fødd i Noreg er det fødelandet til foreldra. I dei tilfella der foreldra har ulikt fødeland, er det fødelandet til mora som blir valt. Viss ikkje personen sjølv eller nokon av foreldra er utenlandsfødt, blir henta landsbakgrunn frå dei første utenlandsfødte ein treffar på i rekkjefølgja mormor, morfar, farmor eller farfar."
    },
    "classification_reference": "91",
    "unit_types": ["01", "05"],
    "subject_fields": ["he04"],
    "contains_special_categories_of_personal_data": false,
    "measurement_type": null,
    "valid_until": "2026-01-01",
    "external_reference_uri": "https://www.ssb.no/a/metadata/conceptvariable/vardok/1919/nb",
    "comment": {
        "en": "Changes in unit types",
        "nb": "Endring i enhetstyper.",
        "nn": "Endring i enhetstyper."
    },
    "related_variable_definition_uris": [
        "https://example.com/"
    ],
    "contact": {
        "title": {
            "en": "Division for population statistics",
            "nb": "Seksjon for befolkningsstatistikk",
            "nn": "Seksjon for befolkningsstatistikk"
        },
        "email": "s320@ssb.no"
    }
}"""

const val VALIDITY_PERIOD_EXAMPLE = """
{
    "name": {
        "en": "Country Background",
        "nb": "Landbakgrunnen",
        "nn": "Landbakgrunnen"
    },
    "definition": {
        "en": "Country background is the mothers birth country.",
        "nb": "For personer født i utlandet er dette mors fødeland.",
        "nn": "For personar fødd i utlandet mors fødeland."
    },
    "classification_reference": "91",
    "unit_types": ["01", "05"],
    "subject_fields": ["he04"],
    "contains_special_categories_of_personal_data": false,
    "measurement_type": null,
    "valid_from": "2026-01-02",
    "external_reference_uri": "https://www.ssb.no/a/metadata/conceptvariable/vardok/1919/nb",
    "comment": {
        "en": "Change in legislation triggers change of definition text.",
        "nb": "Endring i lovgiving utløser endring av definisjonstekst.",
        "nn": "Endring i lovgiving utløser endring av definisjonstekst."
    },
    "related_variable_definition_uris": [
        "https://example.com/"
    ],
    "contact": {
        "title": {
            "en": "Division for population statistics",
            "nb": "Seksjon for befolkningsstatistikk",
            "nn": "Seksjon for befolkningsstatistikk"
        },
        "email": "s320@ssb.no"
    }
}
"""
const val RENDERED_VARIABLE_DEFINITION_EXAMPLE = """{
    "id": "$ID_EXAMPLE",
    "name": "Landbakgrunn",
    "short_name": "string",
    "definition": "For personer født i utlandet, er dette (med noen få unntak) eget fødeland. For personer født i Norge er det foreldrenes fødeland. I de tilfeller der foreldrene har ulikt fødeland, er det morens fødeland som blir valgt. Hvis ikke personen selv eller noen av foreldrene er utenlandsfødt, hentes landbakgrunn fra de første utenlandsfødte en treffer på i rekkefølgen mormor, morfar, farmor eller farfar.",
    "classification_uri": "https://www.ssb.no/en/klass/klassifikasjoner/91",
    "unit_types": [{
            "reference_uri": "https://www.ssb.no/klass/klassifikasjoner/$UNIT_TYPES_KLASS_CODE",
            "code": "20",
            "title": "Person"
        }
    ],
    "subject_fields": [
    $KLASS_REFERENCE_SUBJECT_FIELD_EXAMPLE
    ],
    "contains_special_categories_of_personal_data": true,
    "measurement_type": null,
    "valid_from": "2003-01-01",
    "valid_until": null,
    "external_reference_uri": "https://www.ssb.no/a/metadata/conceptvariable/vardok/1919/nb",
    "comment": "Fra og med 1.1.2003 ble definisjon endret til også å trekke inn besteforeldrenes fødeland.",
    "related_variable_definition_uris": [
        "https://example.com/"
    ],
    "contact": $RENDERED_CONTACT_EXAMPLE,
    "last_updated_at": "2024-06-12T10:39:41.038Z"
}"""

const val LIST_OF_RENDERED_VARIABLE_DEFINITIONS_EXAMPLE = """[
    $RENDERED_VARIABLE_DEFINITION_EXAMPLE
]"""

const val LIST_OF_COMPLETE_RESPONSE_EXAMPLE = """[
    $COMPLETE_RESPONSE_EXAMPLE
]"""

const val VARDOK_ID_RESPONSE_EXAMPLE = """{vardok_id: 25}"""

const val PROBLEM_JSON_NOT_FOUND_EXAMPLE = """{
    "type": "about:blank",
    "title": null,
    "status": 404,
    "detail": "Not found",
    "instance": null,
    "parameters": {   
    }
}"""

const val PROBLEM_JSON_CONFLICT_EXAMPLE = """{
    "type": "about:blank",
    "title": null,
    "status": 409,
    "detail": "Short name is already in use by another variable definition."
}"""

const val PROBLEM_JSON_METHOD_NOT_ALLOWED_EXAMPLE = """{
    "type": "about:blank",
    "title": null,
    "status": 405,
    "detail": "Not allowed for variable definitions with this status."
}"""

const val PROBLEM_JSON_BAD_REQUEST_EXAMPLE = """{
    "type": "about:blank",
    "title": null,
    "status": 400,
    "detail": "Failed to convert argument [example] for value [null] due to: Error deserializing type: example"
}"""

const val PROBLEM_JSON_CONSTRAINT_VIOLATION_EXAMPLE = """{
  "cause": null,
  "suppressed": [],
  "detail": null,
  "instance": null,
  "parameters": {},
  "type": "https://zalando.github.io/problem/constraint-violation",
  "title": "Constraint Violation",
  "status": 400,
  "violations": [
    {
      "field": "classificationReference",
      "message": "Code incorrect is not a valid classification id"
    }
  ]
}"""
