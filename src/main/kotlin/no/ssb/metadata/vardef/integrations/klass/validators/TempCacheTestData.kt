package no.ssb.metadata.vardef.integrations.klass.validators

import no.ssb.metadata.vardef.integrations.klass.models.Classification
import no.ssb.metadata.vardef.integrations.klass.models.ClassificationLinks
import no.ssb.metadata.vardef.integrations.klass.models.Link

/* TODO: Only for testing, remove when cache object is implemented
* */
object TestCacheObjectClassification {
    val classificationItems =
        listOf(
            Classification(
                name = "Standard for delområde- og grunnkretsinndeling",
                id = 1,
                classificationType = "Klassifikasjon",
                lastModified = "2024-03-08T13:55:51.000+0000",
                links = ClassificationLinks(self = Link(href = "https://data.ssb.no/api/klass/v1/classifications/1")),
            ),
            Classification(
                name = "Standard for kjønn",
                id = 2,
                classificationType = "Klassifikasjon",
                lastModified = "2018-12-07T14:02:33.000+0000",
                links = ClassificationLinks(self = Link(href = "https://data.ssb.no/api/klass/v1/classifications/2")),
            ),
            Classification(
                name = "Standard for trafikanttype",
                id = 13,
                classificationType = "Klassifikasjon",
                lastModified = "2016-10-07T12:06:17.000+0000",
                links = ClassificationLinks(self = Link(href = "https://data.ssb.no/api/klass/v1/classifications/13")),
            ),
            Classification(
                name = "Standard for veitrafikkulykke",
                id = 14,
                classificationType = "Klassifikasjon",
                lastModified = "2016-10-07T12:06:17.000+0000",
                links = ClassificationLinks(self = Link("href=https://data.ssb.no/api/klass/v1/classifications/14")),
            ),
        )
}

data class KlassUnitField(
    val unitCode: String,
    val parentCode: String?,
    val level: String?,
    val unitName: String?,
    val shortName: String?,
    val presentationName: String?,
    val validFrom: String?,
    val validTo: String?,
    val notes: String?,
)

data class KlassSubjectField(
    val subjectCode: String,
    val parentCode: String?,
    val level: String?,
    val subjectName: String?,
    val shortName: String?,
    val presentationName: String?,
    val validFrom: String?,
    val validTo: String?,
    val notes: String?,
)

object TestUnitCodes {
    val testDataCodes =
        listOf(
            KlassUnitField(
                "01",
                null,
                "1",
                "Adresse",
                "", "",
                "2023-09-01",
                null,
                "En offisiell adresse er den fullstendige adressen for en bygning, bygningsdel, bruksenhet, " +
                    "eiendom eller et annet objekt som er registrert med adresse i eiendomsregisteret matrikkelen. " +
                    "Hver adresse skal være koordinatfestet.",
            ),
            KlassUnitField(
                "02",
                null,
                "1",
                "Arbeidsulykke",
                "",
                "",
                "2023-09-01",
                null,
                "Arbeidsulykke inkluderer yrkesskader og fatale ulykker på norsk eller utenlandsk landterritorium, " +
                    "i petroleumsvirksomhet til havs og i tjeneste på skip eller under fiske/fangst.",
            ),
            KlassUnitField(
                "03",
                null,
                "1",
                "Bolig",
                "",
                "",
                "2023-09-01",
                null,
                "Bolig er en boenhet bestående av ett eller flere rom som er bygd eller ombygd til helårs " +
                    "privatbolig for en eller flere personer, og som har egen atkomst til rommet/rommene uten at " +
                    "man må gå gjennom annen bolig. En bolig er enten en leilighet eller en hybel. " +
                    "En leilighet er en bolig med minst ett rom og kjøkken (leilighet inkluderer også enebolig, " +
                    "rekkehus osv.). En hybel er et rom med egen inngang beregnet som bolig for en eller flere " +
                    "personer som har adgang til vann og toalett uten at det er nødvendig å gå gjennom en " +
                    "annen leilighet. Dette inkluderer også hybler i hybelbygg med felles inngang og kjøkken, " +
                    "typisk studenthybler og enheter i bofellesskap for eldre, funksjonshemmede o.l. " +
                    "\"Hybler\" uten egen inngang (f.eks. rom i en privatbolig) regnes ikke som bolig. ",
            ),
        )
}

object TestSubjectCodes {
    val testDataCodesSubjectField =
        listOf(
            KlassSubjectField(
                "al",
                null,
                "1",
                "Arbeid og lønn",
                "",
                "",
                null,
                null,
                "",
            ),
            KlassSubjectField(
                "al03",
                "al",
                "2",
                "Arbeidsledighet",
                "",
                "",
                null,
                null,
                "",
            ),
            KlassSubjectField(
                "al04",
                "al",
                "2",
                "Arbeidsmiljø, sykefravær og arbeidskonflikter",
                "",
                "",
                null,
                null,
                "",
            ),
            KlassSubjectField(
                "al05",
                "al",
                "2",
                "Lønn og arbeidskraftkostnader",
                "",
                "",
                null,
                null,
                "",
            ),
            KlassSubjectField(
                "al06",
                "al",
                "2",
                "Sysselsetting",
                "",
                "",
                null,
                null,
                "",
            ),
            KlassSubjectField(
                "bb",
                null,
                "1",
                "Bygg, bolig og eiendom",
                "",
                "",
                null,
                null,
                "",
            ),
        )
}
