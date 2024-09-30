package no.ssb.metadata.vardef.integrations.vardok

import no.ssb.metadata.vardef.models.Owner

val unitTypeConverter =
    mapOf(
        "Adresse" to "01",
        "Arbeidsulykke" to "02",
        "Bolig" to "03",
        "Bygning" to "04",
        "Eiendom" to "05",
        "Familie" to "06",
        "Foretak" to "07",
        "Fylke" to "08",
        "Fylke" to "09",
        "Havneanløp" to "10",
        "Husholdningt" to "11",
        "Kjøretøy" to "12",
        "Kommune (forvaltning)" to "13",
        "Kommune" to "14",
        "Kurs" to "15",
        "Lovbrudd" to "16",
        "Person" to "17",
        "Skip" to "18",
        "Statlig Virksomhet" to "19",
        "Storfe" to "20",
        "Trafikkulykke" to "21",
        "Transaksjon" to "22",
        "Valg" to "23",
        "Vare/tjeneste" to "24",
        "Verdipapir" to "25",
        "Virksomhet" to "26",
    )

fun getValidDates(vardokItem: VardokResponse): Pair<String, String?> {
    val dateString = vardokItem.dc?.valid?.split(" - ")

    val firstDate = dateString?.getOrNull(0)?.trim()?.takeIf { it.isNotEmpty() }
    val secondDate = dateString?.getOrNull(1)?.trim()?.takeIf { it.isNotEmpty() }

    if (firstDate != null)
        {
            return Pair(firstDate, secondDate)
        }
    throw MissingValidDatesException()
}

fun mapVardokIdentifier(vardokItem: VardokResponse): String {
    val vardokId = vardokItem.id
    val splitId = vardokId.split(":")
    return splitId[splitId.size - 1]
}

fun mapVardokContactDivisionToOwner(vardokItem: VardokResponse): Owner {
    val owner = vardokItem.common?.contactDivision
    val mappedOwner = Owner(owner!!.codeValue, owner.codeText)
    return mappedOwner
}
