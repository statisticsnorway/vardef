package no.ssb.metadata.vardef.constants

const val ID_EXAMPLE = """"wypvb3wd""""

const val INPUT_VARIABLE_DEFINITION_EXAMPLE = """{
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
    "contains_sensitive_personal_information": true,
    "measurement_type": null,
    "valid_from": "2003-01-01",
    "valid_until": null,
    "external_reference_uri": "https://www.ssb.no/a/metadata/conceptvariable/vardok/1919/nb",
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

const val FULL_RESPONSE_VARIABLE_DEFINITION_EXAMPLE = """{
    "id": $ID_EXAMPLE,
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
    "contains_sensitive_personal_information": true,
    "variable_status": "DRAFT",
    "measurement_type": null,
    "valid_from": "2003-01-01",
    "valid_until": null,
    "external_reference_uri": "https://www.ssb.no/a/metadata/conceptvariable/vardok/1919/nb",
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

const val KLASS_REFERENCE_SUBJECT_FIELD_EXAMPLE = """{
            "reference_uri": "https://www.ssb.no/klass/klassifikasjoner/618",
            "code": "be07",
            "title": "Innvandrere"
        }"""

const val PERSON_EXAMPLE = """{
        "code": "ano@ssb.no",
        "name": "Ola Nordmann"
    }
"""

const val CONTACT_EXAMPLE = """{
        "title": "Seksjon for befolkningsstatistikk",
        "email": "s320@ssb.no"
    }
"""

const val RENDERED_VARIABLE_DEFINITION_EXAMPLE = """{
    "id": $ID_EXAMPLE,
    "name": "Landbakgrunn",
    "short_name": "string",
    "definition": "For personer født i utlandet, er dette (med noen få unntak) eget fødeland. For personer født i Norge er det foreldrenes fødeland. I de tilfeller der foreldrene har ulikt fødeland, er det morens fødeland som blir valgt. Hvis ikke personen selv eller noen av foreldrene er utenlandsfødt, hentes landbakgrunn fra de første utenlandsfødte en treffer på i rekkefølgen mormor, morfar, farmor eller farfar.",
    "classification_uri": "https://www.ssb.no/en/klass/klassifikasjoner/91",
    "unit_types": [{
            "reference_uri": "https://www.ssb.no/klass/klassifikasjoner/702",
            "code": "17",
            "title": "Person"
        }
    ],
    "subject_fields": [
    $KLASS_REFERENCE_SUBJECT_FIELD_EXAMPLE
    ],
    "contains_sensitive_personal_information": true,
    "variable_status": "PUBLISHED_EXTERNAL",
    "measurement_type": null,
    "valid_from": "2003-01-01",
    "valid_until": null,
    "external_reference_uri": "https://www.ssb.no/a/metadata/conceptvariable/vardok/1919/nb",
    "related_variable_definition_uris": [
        "https://example.com/"
    ],
    "owner": {
        "code": "320",
        "name": "Seksjon for befolkningsstatistikk"
    },
    "contact": $CONTACT_EXAMPLE,
    "created_at": "2024-06-12T10:39:41.038Z",
    "created_by": $PERSON_EXAMPLE,
    "last_updated_at": "2024-06-12T10:39:41.038Z",
    "last_updated_by": $PERSON_EXAMPLE
}
"""

const val LIST_OF_RENDERED_VARIABLE_DEFINITIONS_EXAMPLE = """[
    $RENDERED_VARIABLE_DEFINITION_EXAMPLE
]"""
const val EMPTY_LIST_EXAMPLE = """[]"""

const val DATE_EXAMPLE = "1970-01-01"
