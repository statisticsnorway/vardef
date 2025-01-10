package no.ssb.metadata.vardef.integrations.vardok

import no.ssb.metadata.vardef.integrations.vardok.UnitTypes.Companion.findCategoryForValue
import no.ssb.metadata.vardef.integrations.vardok.models.MissingValidFromException
import no.ssb.metadata.vardef.integrations.vardok.models.OutdatedUnitTypesException
import no.ssb.metadata.vardef.integrations.vardok.models.VardokResponse
import no.ssb.metadata.vardef.models.SupportedLanguages

/**
 * Enum of all official titles for unit types.
 * Set of string values which maps to the title
 */
enum class UnitTypes(val values: Set<String>) {
    ADRESSE(setOf("Adresse")),
    ARBEIDSULYKKE(setOf("Arbeidsulykke")),
    BEDRIFT(setOf("Bedrift", "Virksomhet")),
    BOLIG(setOf("Bolig")),
    BRANSJE_ENHET(setOf("Bransjeenhet")),
    KJOERETOEY(setOf("Kjøretøy")),
    BYGNING(setOf("Bygning")),
    EIENDOM(setOf("Eiendom", "Tinglyst omsetning", "Skogareal", "Landbrukseiendom", "Grunneiendom")),
    FAMILIE(setOf("Familie")),
    FORETAK(setOf("Foretak")),
    FYLKE_FORVALTNING(setOf("Fylke (forvaltning)")),
    FYLKE_GEOGRAFISK(setOf("Fylke (geografisk)")),
    HAVNEANLOEP(setOf("Havneanløp")),
    HUSHOLDNING(setOf("Husholdning")),
    JURIDISK_ENHET(setOf("Juridisk enhet")),
    KURS(setOf("Kurs")),
    KOMMUNE_FORVALTNING(setOf("Kommune (forvaltning)")),
    KOMMUNE_GEOGRAFISK(setOf("Kommune (geografisk)")),
    LOVBRUDD(setOf("Lovbrudd")),
    PERSON(setOf("Arbeidsforhold", "Person")),
    SKIP(setOf("Skip", "Fiskefartøy")),
    STATLIG_VIRKSOMHET(setOf("Statlig virksomhet", "Offentlig forvaltning")),
    STORFE(setOf("Storfe")),
    TRAFIKKULYKKE(setOf("Trafikkulykke", "Ulykke")),
    TRANSAKSJON(setOf("Transaksjon")),
    VALG(setOf("Valg")),
    VARE_TJENESTE(setOf("Vare/tjeneste", "Repr.vare og -tjeneste", "Internett-abonnement")),
    VERDIPAPIR(setOf("Verdipapir")),
    SAK(setOf("Sak")),
    AVFALL_ANLOEP_LUFTFARTOEY(setOf("Avfallsanlegg", "Avløpsanlegg", "Luftfartøy")),
    ;

    companion object {
        // Method to find the category for a given value
        fun findCategoryForValue(value: String): UnitTypes? {
            return entries.find { category -> value in category.values }
        }
    }
}

/**
 * This converter map statisticalUnit (title) with unitType codes.
 * Some statisticalUnits are outdated and are mapped according to set rules.
 *
 * @param name: statisticalUnit in Vardok
 * @return A list of codes
 */
fun convertUnitTypes(name: String): List<String?> =
    when (UnitTypes.findCategoryForValue(name)) {
        UnitTypes.ADRESSE -> listOf("01")
        UnitTypes.ARBEIDSULYKKE -> listOf("02")
        UnitTypes.BOLIG -> listOf("03")
        UnitTypes.BYGNING -> listOf("04")
        UnitTypes.EIENDOM -> listOf("05")
        UnitTypes.FAMILIE -> listOf("06")
        UnitTypes.FYLKE_FORVALTNING -> listOf("07")
        UnitTypes.FYLKE_GEOGRAFISK -> listOf("08")
        UnitTypes.HAVNEANLOEP -> listOf("09")
        UnitTypes.HUSHOLDNING -> listOf("10")
        UnitTypes.JURIDISK_ENHET -> listOf("11")
        UnitTypes.FORETAK -> listOf("12")
        UnitTypes.BEDRIFT -> listOf("13")
        UnitTypes.BRANSJE_ENHET -> listOf("14")
        UnitTypes.KJOERETOEY -> listOf("15")
        UnitTypes.KOMMUNE_FORVALTNING -> listOf("16")
        UnitTypes.KOMMUNE_GEOGRAFISK -> listOf("17")
        UnitTypes.KURS -> listOf("18")
        UnitTypes.LOVBRUDD -> listOf("19")
        UnitTypes.PERSON -> listOf("20")
        UnitTypes.SKIP -> listOf("21")
        UnitTypes.STATLIG_VIRKSOMHET -> listOf("22")
        UnitTypes.STORFE -> listOf("23")
        UnitTypes.TRAFIKKULYKKE -> listOf("24")
        UnitTypes.TRANSAKSJON -> listOf("25")
        UnitTypes.VALG -> listOf("26")
        UnitTypes.VARE_TJENESTE -> listOf("27")
        UnitTypes.VERDIPAPIR -> listOf("28")
        UnitTypes.SAK -> listOf("05", "13")
        UnitTypes.AVFALL_ANLOEP_LUFTFARTOEY -> listOf("12", "13")
        else -> emptyList()
    }

fun getValidDates(vardokItem: VardokResponse): Pair<String, String?> {
    val dateString = vardokItem.dc?.valid?.split(" - ")

    val firstDate = dateString?.getOrNull(0)?.trim()?.takeIf { it.isNotEmpty() }
    val secondDate = dateString?.getOrNull(1)?.trim()?.takeIf { it.isNotEmpty() }

    if (firstDate != null) {
        return Pair(firstDate, secondDate)
    }
    throw MissingValidFromException(vardokItem.id.substringAfterLast(":"))
}

fun mapVardokIdentifier(vardokItem: VardokResponse): String {
    val vardokId = vardokItem.id
    val splitId = vardokId.split(":")
    return splitId[splitId.size - 1]
}

fun mapVardokStatisticalUnitToUnitTypes(vardokItem: VardokResponse): List<String?> {
    val statisticalUnit = vardokItem.variable?.statisticalUnit
    if (statisticalUnit != null && findCategoryForValue(statisticalUnit) != null) {
        return convertUnitTypes(statisticalUnit)
    }

    throw OutdatedUnitTypesException(vardokItem.id.substringAfterLast(":"))
}

/**
 * Maps the `notes` and `calculation` fields of a `VardokItem` to a `LanguageStringType` object.
 *
 * This function processes the `common.notes` and `variable.calculation` fields in the `VardokResponse` object
 * for each language (`nb`, `nn`, `en`) based on the following rules:
 *
 * - If both `notes` and `calculation` are empty, it assigns `null`.
 * - If `notes` is empty and `calculation` is not empty, it assigns the value of `calculation`.
 * - If `calculation` is empty and `notes` is not empty, it assigns the value of `notes`.
 * - If both `notes` and `calculation` are non-empty, it concatenates them and assigns the result.
 *
 * The function preserves existing values for each language while processing only the relevant fields.
 *
 * @param vardokItem A map where the keys are language codes (`nb`, `nn`, `en`) and the values are `VardokResponse` objects.
 * @return A `LanguageStringType` object containing the mapped comment strings for each language.
 */
fun mapVardokComment(vardokItem: Map<String, VardokResponse>): MutableMap<String, String?> {
    val languageComments = mutableMapOf<String, String?>()
    for (language in SupportedLanguages.entries) {
        val notes = vardokItem[language.toString()]?.common?.notes
        val calculation = vardokItem[language.toString()]?.variable?.calculation

        val commentByLanguage =
            when {
                notes.isNullOrEmpty() && calculation.isNullOrEmpty() -> null
                notes.isNullOrEmpty() -> calculation
                calculation.isNullOrEmpty() -> notes
                else -> notes + calculation
            }
        languageComments[language.toString()] = commentByLanguage
    }
    return languageComments
}
