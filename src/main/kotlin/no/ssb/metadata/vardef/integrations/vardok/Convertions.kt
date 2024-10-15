package no.ssb.metadata.vardef.integrations.vardok

val unitTypeConverter =
    mapOf(
        "Adresse" to "01",
        "Arbeidsulykke" to "02",
        "Bolig" to "03",
        "Bygning" to "04",
        "Tinglyst omsetning" to "05",
        "Skogareal" to "05",
        "Landbrukseiendom" to "05",
        "Grunneiendom" to "05",
        "Eiendom" to "05",
        "Familie" to "06",
        "Fylke (forvaltning)" to "07",
        "Fylke (geografisk)" to "08",
        "Havneanløp" to "09",
        "Husholdning" to "10",
        "Juridisk enhet" to "11",
        "Foretak" to "12",
        "Virksomhet" to "13",
        "Bedrift" to "13",
        "Bransjeenhet" to "14",
        "Kjøretøy" to "15",
        "Kommune (forvaltning)" to "16",
        "Kommune (geografisk)" to "17",
        "Kurs" to "18",
        "Lovbrudd" to "19",
        "Arbeidsforhold" to "20",
        "Person" to "20",
        "Fiskefartøy" to "21",
        "Skip" to "21",
        "Offentlig forvaltning" to "22",
        "Statlig Virksomhet" to "22",
        "Storfe" to "23",
        "Ulykke" to "24",
        "Trafikkulykke" to "24",
        "Transaksjon" to "25",
        "Valg" to "26",
        "Repr.vare og -tjeneste" to "27",
        "Internett-abonnement" to "27",
        "Vare/tjeneste" to "27",
        "Verdipapir" to "28",
    )

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
    if (statisticalUnit != null && unitTypeConverter.contains(statisticalUnit)) {
        return listOf(unitTypeConverter[statisticalUnit])
    }

    throw OutdatedUnitTypesException(vardokItem.id.substringAfterLast(":"))
}
