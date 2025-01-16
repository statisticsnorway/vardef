package no.ssb.metadata.vardef.integrations.vardok.convertions

/**
 * Enum of all official titles for unit types.
 * Set of string values which maps to the title
 */
enum class UnitTypes(
    val values: Set<String>,
) {
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
        fun findCategoryForValue(value: String): UnitTypes? = entries.find { category -> value in category.values }
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

/**
 * Set of string values which maps to the subject filed value
 */
val SubjectAreaMapping =
    mapOf(
        setOf(
            "Regionale",
            "Befolkning",
            "Befolkningsstruktur",
            "Folkemengde",
            "Familier, husholdninger",
            "Befolkningsendringer",
            "Region -",
        ) to "be",
        setOf(
            "Valg",
            "Stortingsvalg",
            "Fylkestings- og kommunestyrevalg",
        ) to "va",
        setOf(
            "Tidsbruk",
        ) to "kf",
        setOf(
            "Arealbruk",
            "Luft",
            "Vann",
            "Avfall",
            "Miljøvernkostnader",
            "Miljøregnskap",
        ) to "nm",
        setOf(
            "Energi",
            "Ordretilgang, ordrereserver",
            "Omsetning og produksjon",
            "Investering",
            "Produksjonsindekser",
            "Utvinning av råolje og naturgass",
            "Produksjon av nærings- og nytelsesmidler",
            "Elektrisitets-, gass-, damp- og varmtvannsforsyning",
        ) to "ei",
        setOf(
            "Helse, sosiale forhold og kriminalitet",
            "Helsetjenester",
            "Sykehustjenester",
            "Legetjenester",
            "Andre helsetjenester",
        ) to "he",
        setOf(
            "Barnevern",
            "Sosiale tjenester, trygd og sosialhjelp",
            "Trygd og sosialhjelp",
            "Kriminalitet og rettsvesen",
        ) to "sk",
        setOf(
            "Utdanning",
            "Utdanningsnivå",
            "Utdanningsinstitusjoner",
            "Barnehager, førskoler",
            "Universitet og høgskoler",
            "Voksenopplæring og annen undervisning",
        ) to "ud",
        setOf(
            "Personlig økonomi og boforhold",
            "Inntekt, formue, skatt",
            "Inntekt",
            "Formue",
            "Skatt",
            "Forbruk",
        ) to "if",
        setOf(
            "Bolig, boforhold",
            "Bygge- og anleggsvirksomhet",
            "Omsetning og drift av fast eiendom",
        ) to "bb",
        setOf(
            "Arbeidsliv, yrkesdeltaking og lønn",
            "Yrkesdeltaking",
            "Arbeidsmiljø, sykefravær",
            "Arbeidsledighet",
            "Arbeidskonflikter",
            "Lønn, arbeidskraftkostnader",
        ) to "al",
        setOf(
            "Fritidsvirksomhet, kulturell tjenesteyting og sport",
            "Medlemskap i organisasjoner, tros- og livssynssamfunn",
        ) to "kf",
        setOf(
            "Priser, prisindekser og konjunkturindikatorer",
            "Pris- og kostnadsindekser, prisnivåsammenligninger",
            "Konsumprisindeks",
            "Produsentprisindekser - varer og tjenester",
            "Boligprisindekser",
        ) to "pp",
        setOf(
            "Nasjonalregnskap og utenrikshandel",
            "Nasjonalregnskap",
            "Finansielle sektorbalanser",
        ) to "nk",
        setOf(
            "Utenriksregnskap",
            "Fordringer og gjeld overfor utlandet, direkteinvesteringer, SIFON",
            "Utenrikshandel",
        ) to "ut",
        setOf(
            "Næringsvirksomhet",
            "Struktur, bedriftregister",
            "Skatt næringsvirksomhet",
        ) to "vf",
        setOf(
            "Teknologiske indikatorer, inkl. IKT",
        ) to "ti",
        setOf(
            "Jordbruk og skogbruk",
            "Skogbruk",
            "Fiske og fiskeoppdrett",
        ) to "js",
        setOf(
            "Varehandel",
        ) to "vt",
        setOf(
            "Hotell- og restaurantvirksomhet",
            "Transport og kommunikasjon",
            "Veitransport",
            "Sjøtransport",
            "Lufttransport",
            "Tjenester tilknyttet transport og reisevirksomhet",
        ) to "tr",
        setOf(
            "Finansielle foretak",
            "Finansinstitusjoner",
            "Andre finansieller foretak",
            "Fonds- og aksjemegling, aksje- og obligasjonsfond, børser, legater",
            "Aksjer, obligasjoner, seritfikater og øvrig kredittmarked",
        ) to "bf",
        setOf(
            "Offentlige finanser",
            "Kommunale finanser",
        ) to "os",
    )

fun convertSubjectArea(name: String): String? =
    SubjectAreaMapping.entries
        .firstOrNull { entry -> name in entry.key }
        ?.value
