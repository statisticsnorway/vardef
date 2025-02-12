package no.ssb.metadata.vardef.models

import io.micronaut.serde.bson.BsonJsonMapper
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.argumentSet
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

@MicronautTest(startApplication = false)
class MongodbDeserializeTest {
    @Inject
    lateinit var bsonJsonMapper: BsonJsonMapper

    /**
     * WARNING: If this test fails, consider the change BREAKING.
     *
     * You should perform a database migration to avoid breaking the app.
     */
    @ParameterizedTest
    @MethodSource("savedVariableDefinitionVariations")
    fun `deserialize saved variable definition`(serializedJson: String) {
        Assertions.assertInstanceOf(
            SavedVariableDefinition::class.java,
            bsonJsonMapper.readValue(serializedJson, SavedVariableDefinition::class.java),
        )
    }

    companion object {
        @JvmStatic
        fun savedVariableDefinitionVariations(): Stream<Arguments> =
            Stream.of(
                argumentSet(
                    "Nullable values present",
                    """{
  "definitionId": "_JMd84Ch",
  "patchId": 1,
  "name": {
    "nb": "Landbakgrunn",
    "nn": "Landbakgrunn",
    "en": "Country Background"
  },
  "shortName": "landbakk",
  "definition": {
    "nb": "For personer født i utlandet, er dette (med noen få unntak) eget fødeland. For personer født i Norge er det foreldrenes fødeland. I de tilfeller der foreldrene har ulikt fødeland, er det morens fødeland som blir valgt. Hvis ikke personen selv eller noen av foreldrene er utenlandsfødt, hentes landbakgrunn fra de første utenlandsfødte en treffer på i rekkefølgen mormor, morfar, farmor eller farfar.",
    "nn": "For personar fødd i utlandet, er dette (med nokre få unntak) eige fødeland. For personar fødd i Noreg er det fødelandet til foreldra. I dei tilfella der foreldra har ulikt fødeland, er det fødelandet til mora som blir valt. Viss ikkje personen sjølv eller nokon av foreldra er utenlandsfødt, blir henta landsbakgrunn frå dei første utenlandsfødte ein treffar på i rekkjefølgja mormor, morfar, farmor eller farfar.",
    "en": "Country background is the person's own, the mother's or possibly the father's country of birth. Persons without an immigrant background always have Norway as country background. In cases where the parents have different countries of birth the mother's country of birth is chosen. If neither the person nor the parents are born abroad, country background is chosen from the first person born abroad in the order mother's mother, mother's father, father's mother, father's father."
  },
  "classificationReference": "91",
  "unitTypes": [
    "01",
    "02"
  ],
  "subjectFields": [
    "he04"
  ],
  "containsSpecialCategoriesOfPersonalData": false,
  "variableStatus": "DRAFT",
  "measurementType": "01",
  "validFrom": "2011-12-03",
  "validUntil": "2020-12-03",
  "externalReferenceUri": "https://www.ssb.no/a/metadata/conceptvariable/vardok/1919/nb",
  "comment": {
    "nb": "Fra og med 1.1.2003 ble definisjon endret til også å trekke inn besteforeldrenes fødeland.",
    "nn": "Fra og med 1.1.2003 ble definisjon endret til også å trekke inn besteforeldrenes fødeland.",
    "en": "As of 1 January 2003, the definition was changed to also include the grandparents' country of birth."
  },
  "relatedVariableDefinitionUris": [
    "https://example.com/"
  ],
  "owner": {
    "team": "dapla-felles",
    "groups": [
      "dapla-felles-developers"
    ]
  },
  "contact": {
    "title": {
      "nb": "Seksjon for befolkningsstatistikk",
      "nn": "Seksjon for befolkningsstatistikk",
      "en": "Division for population statistics"
    },
    "email": "s320@ssb.no"
  },
  "createdAt": "2025-02-06T11:05:05Z",
  "createdBy": "mmw@ssb.no",
  "lastUpdatedAt": "2025-02-06T11:05:05Z",
  "lastUpdatedBy": "mmw@ssb.no"
}""",
                ),
                argumentSet(
                    "Nullable values null",
                    """{
  "definitionId": "_JMd84Ch",
  "patchId": 1,
  "name": {
    "nb": "Landbakgrunn",
    "nn": "Landbakgrunn",
    "en": "Country Background"
  },
  "shortName": "landbakk",
  "definition": {
    "nb": "For personer født i utlandet, er dette (med noen få unntak) eget fødeland. For personer født i Norge er det foreldrenes fødeland. I de tilfeller der foreldrene har ulikt fødeland, er det morens fødeland som blir valgt. Hvis ikke personen selv eller noen av foreldrene er utenlandsfødt, hentes landbakgrunn fra de første utenlandsfødte en treffer på i rekkefølgen mormor, morfar, farmor eller farfar.",
    "nn": "For personar fødd i utlandet, er dette (med nokre få unntak) eige fødeland. For personar fødd i Noreg er det fødelandet til foreldra. I dei tilfella der foreldra har ulikt fødeland, er det fødelandet til mora som blir valt. Viss ikkje personen sjølv eller nokon av foreldra er utenlandsfødt, blir henta landsbakgrunn frå dei første utenlandsfødte ein treffar på i rekkjefølgja mormor, morfar, farmor eller farfar.",
    "en": "Country background is the person's own, the mother's or possibly the father's country of birth. Persons without an immigrant background always have Norway as country background. In cases where the parents have different countries of birth the mother's country of birth is chosen. If neither the person nor the parents are born abroad, country background is chosen from the first person born abroad in the order mother's mother, mother's father, father's mother, father's father."
  },
  "classificationReference": null,
  "unitTypes": [
    "01",
    "02"
  ],
  "subjectFields": [
    "he04"
  ],
  "containsSpecialCategoriesOfPersonalData": false,
  "variableStatus": "DRAFT",
  "measurementType": null,
  "validFrom": "2011-12-03",
  "validUntil": null,
  "externalReferenceUri": null,
  "comment": null,
  "relatedVariableDefinitionUris": null,
  "owner": {
    "team": "dapla-felles",
    "groups": [
      "dapla-felles-developers"
    ]
  },
  "contact": {
    "title": {
      "nb": "Seksjon for befolkningsstatistikk",
      "nn": "Seksjon for befolkningsstatistikk",
      "en": "Division for population statistics"
    },
    "email": "s320@ssb.no"
  },
  "createdAt": "2025-02-06T11:05:05Z",
  "createdBy": "mmw@ssb.no",
  "lastUpdatedAt": "2025-02-06T11:05:05Z",
  "lastUpdatedBy": "mmw@ssb.no"
}""",
                ),
            )
    }
}
