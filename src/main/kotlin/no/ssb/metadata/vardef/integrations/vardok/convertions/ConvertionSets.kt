package no.ssb.metadata.vardef.integrations.vardok.convertions

/**
 * Set of string values which maps from *StatisticalUnit* in Vardok to unittypes list
 *
 * For more information about unit type mapping from Vardok to Vardef see
 * [Migreringsplan- enhetstyper](https://statistics-norway.atlassian.net/wiki/spaces/DAPLA/pages/4128276501/Migreringsplan+fra+Vardok+til+Vardef#Enhetstyper)
 */
val unitTypesMapping =
    mapOf(
        setOf("Adresse") to listOf("01"),
        setOf("Arbeidsulykke") to listOf("02"),
        setOf("Bolig") to listOf("03"),
        setOf("Bygning") to listOf("04"),
        setOf(
            "Eiendom",
            "Tinglyst omsetning",
            "Skogareal",
            "Landbrukseiendom",
            "Grunneiendom",
        ) to listOf("05"),
        setOf("Familie") to listOf("06"),
        setOf("Fylke (forvaltning)") to listOf("07"),
        setOf("Fylke (geografisk)") to listOf("08"),
        setOf("Havneanløp") to listOf("09"),
        setOf("Husholdning") to listOf("10"),
        setOf("Juridisk enhet") to listOf("11"),
        setOf("Foretak") to listOf("12"),
        setOf(
            "Bedrift",
            "Virksomhet",
        ) to listOf("13"),
        setOf("Bransjeenhet") to listOf("14"),
        setOf("Kjøretøy") to listOf("15"),
        setOf("Kommune (forvaltning)") to listOf("16"),
        setOf("Kommune (geografisk)") to listOf("17"),
        setOf("Kurs") to listOf("18"),
        setOf("Lovbrudd") to listOf("19"),
        setOf(
            "Arbeidsforhold",
            "Person",
        ) to listOf("20"),
        setOf(
            "Skip",
            "Fiskefartøy",
        ) to listOf("21"),
        setOf(
            "Statlig virksomhet",
            "Offentlig forvaltning",
        ) to listOf("22"),
        setOf("Storfe") to listOf("23"),
        setOf("Trafikkulykke", "Ulykke") to listOf("24"),
        setOf("Transaksjon") to listOf("25"),
        setOf("Valg") to listOf("26"),
        setOf(
            "Vare/tjeneste",
            "Repr.vare og -tjeneste",
            "Internett-abonnement",
        ) to listOf("27"),
        setOf("Verdipapir") to listOf("28"),
        setOf(
            "Arbeidskonflikt",
            "Avfallsanlegg",
            "Avløpsanlegg",
            "Luftfartøy",
            "Sak",
        ) to listOf("12", "13"),
    )

fun convertUnitTypes(name: String): List<String>? =
    unitTypesMapping.entries
        .firstOrNull { entry -> name in entry.key }
        ?.value

/**
 * This mapping covers variable definitions from Vardok identified by id
 * where *StatisticalUnit* shall not be mapped according to existing mapping of unittypes
 *
 *  For more information about unit type mapping from Vardok to Vardef see
 *  [Migreringsplan- Enhetstyper](https://statistics-norway.atlassian.net/wiki/spaces/DAPLA/pages/4128276501/Migreringsplan+fra+Vardok+til+Vardef#Enhetstyper).
 *
 * @see unitTypesMapping
 *
 */
fun specialCaseUnitMapping(vardokId: String): List<String>? =
    when (vardokId) {
        "2124", "2141", "2142" -> listOf("04")
        "2139", "2149", "2206", "2148" -> listOf("05")
        "1431", "2092", "3246", "1943" -> listOf("12", "13")
        "1700", "1712", "1713", "1319", "1320" -> listOf("16")
        "1416", "2097", "1701", "1321", "2190" -> listOf("20")
        "1997", "2129", "2130", "2133", "2192", "2193" -> listOf("29")
        "2216", "2217", "2210", "2218" -> listOf("01", "04", "05")
        "2182" -> listOf("12", "20")
        "3125" -> listOf("21")
        else -> null
    }

/**
 * Set of string values which maps to the subject field value
 *
 * For more information about subject area mapping from Vardok to Vardef see
 * [Migreringsplan- Statistikkområde](https://statistics-norway.atlassian.net/wiki/spaces/DAPLA/pages/4128276501/Migreringsplan+fra+Vardok+til+Vardef#Statistikkomr%C3%A5de)
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
            "Jordbruk, jakt, viltstell",
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
