package no.ssb.metadata.constants

const val INPUT_VARIABLE_DEFINITION_EXAMPLE = """
{
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
    "contains_unit_identifying_information": true,
    "contains_sensitive_personal_information": true,
    "variable_status": "DRAFT",
    "measurement_type": "02.01",
    "valid_from": "2024-06-05",
    "valid_until": "2024-06-05",
    "external_reference_uri": "https://example.com/",
    "related_variable_definition_uris": [
        "https://example.com/"
    ],
    "contact": {
        "title": {
            "en": "string",
            "nb": "string",
            "nn": "string"
        },
        "email": "user@example.com"
    }
}
"""

const val RENDERED_VARIABLE_DEFINITION_EXAMPLE = """{
    "id": "wypvb3wd",
    "name": "Landbakgrunn",
    "short_name": "string",
    "definition": "For personer født i utlandet, er dette (med noen få unntak) eget fødeland. For personer født i Norge er det foreldrenes fødeland. I de tilfeller der foreldrene har ulikt fødeland, er det morens fødeland som blir valgt. Hvis ikke personen selv eller noen av foreldrene er utenlandsfødt, hentes landbakgrunn fra de første utenlandsfødte en treffer på i rekkefølgen mormor, morfar, farmor eller farfar.",
    "classification_uri": "https://www.ssb.no/en/klass/klassifikasjoner/91",
    "unit_types": [{
            "reference_uri": "https://example.com/",
            "code": "20",
            "title": "Storfe"
        }
    ],
    "subject_fields": [{
            "reference_uri": "https://example.com/",
            "code": "sk",
            "title": "Sosiale forhold og kriminalitet"
        }
    ],
    "contains_unit_identifying_information": true,
    "contains_sensitive_personal_information": true,
    "variable_status": "DRAFT",
    "measurement_type": {
        "reference_uri": "https://example.com/",
        "code": "07",
        "title": "Effekt"
    },
    "valid_from": "2024-06-05",
    "valid_until": "2024-06-05",
    "external_reference_uri": "https://example.com/",
    "related_variable_definition_uris": [
        "https://example.com/"
    ],
    "owner": {
        "code": "724",
        "name": "Dataplattform"
    },
    "contact": {
        "title": "",
        "email": "user@example.com"
    },
    "created_at": "2024-06-12T10:39:41.038Z",
    "created_by": {
        "code": "",
        "name": ""
    },
    "last_updated_at": "2024-06-12T10:39:41.038Z",
    "last_updated_by": {
        "code": "",
        "name": ""
    }
}
"""
