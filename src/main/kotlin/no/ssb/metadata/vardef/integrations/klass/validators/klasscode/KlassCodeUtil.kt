package no.ssb.metadata.vardef.integrations.klass.validators.klasscode

import com.mongodb.internal.authentication.AwsCredentialHelper.LOGGER

object KlassCodeUtil {
    fun isValidCode(values: List<String>): Boolean {
        val unitCodes = TestCodes.testDataCodes.map { it.unitCode }

        val hasValidCodes = values.any { code -> unitCodes.contains(code) }
        val hasInvalidCodes = values.any { code -> !unitCodes.contains(code) }

        LOGGER.info("Valid values: $hasValidCodes, invalid values: $hasInvalidCodes")

        return hasValidCodes && !hasInvalidCodes
    }
}

class KlassUnitField(
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

object TestCodes {
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
