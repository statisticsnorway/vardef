package no.ssb.metadata.vardef.integrations.vardok

// List of names that can be mapped to unitType codes
val statisticalUnitNames =
    listOf(
        "Adresse",
        "Arbeidsulykke",
        "Bolig",
        "Bygning",
        "Tinglyst omsetning",
        "Skogareal",
        "Landbrukseiendom",
        "Grunneiendom",
        "Eiendom",
        "Familie",
        "Fylke (forvaltning)",
        "Fylke (geografisk)",
        "Havneanløp",
        "Husholdning",
        "Juridisk enhet",
        "Foretak",
        "Virksomhet",
        "Bedrift",
        "Bransjeenhet",
        "Kjøretøy",
        "Kommune (forvaltning)",
        "Kommune (geografisk)",
        "Kurs",
        "Lovbrudd",
        "Arbeidsforhold",
        "Person",
        "Fiskefartøy",
        "Skip",
        "Offentlig forvaltning",
        "Statlig Virksomhet",
        "Storfe",
        "Ulykke",
        "Trafikkulykke",
        "Transaksjon",
        "Valg",
        "Repr.vare og -tjeneste",
        "Internett-abonnement",
        "Vare/tjeneste",
        "Verdipapir",
        "Avfallsanlegg",
        "Avløpsanlegg",
        "Luftfartøy",
        "sak",
    )

/**
 * This converter map statisticalUnit (title) with unitType codes.
 * Some statisticalUnits are outdated and are mapped according to set rules.
 *
 * @param name: statisticalUnit in Vardok
 * @return A list of codes
 */
fun convertUnitTypes(name: String): List<String?> {
    val unitTypes = mutableListOf<String?>()
    when (name) {
        "Adresse" -> unitTypes.add("01")
        "Arbeidsulykke" -> unitTypes.add("02")
        "Bolig" -> unitTypes.add("03")
        "Bygning" -> unitTypes.add("04")
        in "Tinglyst omsetning", "Skogareal", "Landbrukseiendom", "Grunneiendom", "Eiendom" -> unitTypes.add("05")
        "Familie" -> unitTypes.add("06")
        "Fylke (forvaltning)" -> unitTypes.add("07")
        "Fylke (geografisk)" -> unitTypes.add("08")
        "Havneanløp" -> unitTypes.add("09")
        "Husholdning" -> unitTypes.add("10")
        "Juridisk enhet" -> unitTypes.add("11")
        "Foretak" -> unitTypes.add("12")
        in "Virksomhet", "Bedrift" -> unitTypes.add("13")
        "Bransjeenhet" -> unitTypes.add("14")
        "Kjøretøy" -> unitTypes.add("15")
        "Kommune (forvaltning)" -> unitTypes.add("16")
        "Kommune (geografisk)" -> unitTypes.add("17")
        "Kurs" -> unitTypes.add("18")
        "Lovbrudd" -> unitTypes.add("19")
        in "Arbeidsforhold", "Person" -> unitTypes.add("20")
        in "Fiskefartøy", "Skip" -> unitTypes.add("21")
        in "Offentlig forvaltning", "Statlig Virksomhet" -> unitTypes.add("22")
        "Storfe" -> unitTypes.add("23")
        in "Ulykke", "Trafikkulykke" -> unitTypes.add("24")
        "Transaksjon" -> unitTypes.add("25")
        "Valg" -> unitTypes.add("26")
        in "Repr.vare og -tjeneste", "Internett-abonnement", "Vare/tjeneste" -> unitTypes.add("27")
        "Verdipapir" -> unitTypes.add("28")
        in "Avfallsanlegg", "Avløpsanlegg", "Luftfartøy" -> unitTypes.addAll(listOf("12", "13"))
        "sak" -> unitTypes.addAll(listOf("05", "13"))
    }
    return unitTypes
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
    if (statisticalUnit != null && statisticalUnitNames.contains(statisticalUnit)) {
        return convertUnitTypes(statisticalUnit)
    }

    throw OutdatedUnitTypesException(vardokItem.id.substringAfterLast(":"))
}
