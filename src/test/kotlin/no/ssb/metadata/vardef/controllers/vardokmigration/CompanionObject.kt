package no.ssb.metadata.vardef.controllers.vardokmigration

import no.ssb.metadata.vardef.constants.GENERATED_CONTACT_KEYWORD
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.argumentSet
import java.net.URI
import java.net.URL
import java.util.stream.Stream

class CompanionObject {
    companion object {
        @JvmStatic
        fun newNorwegianUnitTypes(): Stream<Arguments> =
            Stream.of(
                argumentSet(
                    "Verksemd",
                    "2413",
                    "13",
                ),
                argumentSet(
                    "Hushald",
                    "3135",
                    "10",
                ),
            )

        @JvmStatic
        fun newNorwegianMultilanguageFields(): Stream<Arguments> =
            Stream.of(
                argumentSet(
                    "Id 2413",
                    "2413",
                    "Sum utgifter",
                    "Sum av utgifter til løn, innkjøp, refusjon og overføringar.",
                    "${GENERATED_CONTACT_KEYWORD}_tittel",
                ),
                argumentSet(
                    "Id 3135",
                    "3135",
                    "Egenbetaling, barnehagar",
                    "Hushalds utgifter til barnehageplass i kommunale og private barnehagar",
                    "${GENERATED_CONTACT_KEYWORD}_tittel",
                ),
            )

        @JvmStatic
        fun mapExternalDocument(): Stream<Arguments> =
            Stream.of(
                argumentSet(
                    "Vardok id 2 has external document",
                    "2",
                    "http://www.ssb.no/emner/05/90/notat_200372/notat_200372.pdf",
                ),
                argumentSet(
                    "Vardok id 130 has not external document",
                    "130",
                    null,
                ),
                argumentSet(
                    "Vardok id 123 has external document",
                    "123",
                    "http://www.ssb.no/emner/02/01/10/innvbef/om.html",
                ),
                argumentSet(
                    "Vardok id 1245 has invalid external document",
                    "1245",
                    null,
                ),
            )

        @JvmStatic
        fun mapConceptVariableRelations(): Stream<Arguments> =
            Stream.of(
                argumentSet(
                    "Vardok id 2 has several ConceptVariableRelations",
                    "2",
                    listOf(
                        "http://www.ssb.no/conceptvariable/vardok/571",
                        "http://www.ssb.no/conceptvariable/vardok/49",
                        "http://www.ssb.no/conceptvariable/vardok/10",
                        "http://www.ssb.no/conceptvariable/vardok/12",
                        "http://www.ssb.no/conceptvariable/vardok/11",
                    ).map { URI(it).toURL() },
                ),
                argumentSet(
                    "Vardok id 948 has none ConceptVariableRelations",
                    "948",
                    listOf<URL?>(),
                ),
                argumentSet(
                    "Vardok id 1245 has one ConceptVariableRelation",
                    "1245",
                    listOf(
                        "http://www.ssb.no/conceptvariable/vardok/1246",
                    ).map { URI(it).toURL() },
                ),
            )
    }
}