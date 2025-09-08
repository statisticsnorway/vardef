package no.ssb.metadata.vardef.integrations.vardok.conversions

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
        setOf(
            "Husholdning",
            "Hushald",
        ) to listOf("10"),
        setOf("Juridisk enhet") to listOf("11"),
        setOf("Foretak") to listOf("12"),
        setOf(
            "Bedrift",
            "Virksomhet",
            "Verksemd",
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
        "2157", "2159", "2183" -> listOf("01")
        "2124", "2141", "2142" -> listOf("04")
        "1704", "2139", "2149", "2206", "2148" -> listOf("05")
        "747" -> listOf("07")
        "141", "750", "1616" -> listOf("08")
        "1431", "2092", "3246", "1943" -> listOf("12", "13")
        "1319", "1320", "1330", "1700", "1710", "1712", "1713", "1772", "1858", "1861", "1935", "2594", "2598",
        "2601", "3218", "3517",
        -> listOf("16")
        "135", "2590" -> listOf("17")
        "1416", "2097", "1701", "1321", "2190" -> listOf("20")
        "3125" -> listOf("21")
        "1997", "2129", "2130", "2133", "2192", "2193", "2194" -> listOf("29")
        "2216", "2217", "2210", "2218" -> listOf("01", "04", "05")
        "3519", "3520", "3524" -> listOf("07", "08")
        "136", "137", "925", "926", "927", "928", "3518", "3521", "3522", "3523", "1326" -> listOf("16", "17")
        "2182" -> listOf("12", "20")
        "590" -> listOf("12", "13", "20")
        else -> null
    }

enum class StatisticalSubjects(
    val code: String,
) {
    LABOUR_MARKET_AND_EARNINGS("al"),
    CONSTRUCTION_HOUSING_AND_PROPERTY("bb"),
    POPULATION("be"),
    BANKING_AND_FINANCIAL_MARKETS("bf"),
    ENERGY_AND_MANUFACTURING("ei"),
    HEALTH("he"),
    INCOME_AND_CONSUMPTION("if"),
    AGRICULTURE_FORESTRY_HUNTING_AND_FISHING("js"),
    CULTURE_AND_RECREATION("kf"),
    NATIONAL_ACCOUNTS_AND_BUSINESS_CYCLES("nk"),
    NATURE_AND_THE_ENVIRONMENT("nm"),
    PUBLIC_SECTOR("os"),
    PRICES_AND_PRICE_INDICES("pp"),
    SOCIAL_CONDITIONS_WELFARE_AND_CRIME("sk"),
    TECHNOLOGY_AND_INNOVATION("ti"),
    TRANSPORT_AND_TOURISM("tr"),
    EDUCATION("ud"),
    EXTERNAL_ECONOMY("ut"),
    ELECTIONS("va"),
    ESTABLISHMENTS_ENTERPRISES_AND_ACCOUNTS("vf"),
    WHOLESALE_AND_RETAIL_TRADE_AND_SERVICE_ACTIVITIES("vt"),
}

/**
 * Map of the Statistical Subject as used in Vardef to the various SubjectArea names as used in VarDok.
 *
 * For more information about subject area mapping from Vardok to Vardef see
 * [Migreringsplan- Statistikkområde](https://statistics-norway.atlassian.net/wiki/spaces/DAPLA/pages/4128276501/Migreringsplan+fra+Vardok+til+Vardef#Statistikkomr%C3%A5de)
 */
val SubjectAreaMapping: Map<StatisticalSubjects, Set<String>> =
    mapOf(
        StatisticalSubjects.POPULATION to
            setOf(
                "Regionale",
                "Befolkning",
                "Befolkningsstruktur",
                "Folkemengde",
                "Familier, husholdninger",
                "Befolkningsendringer",
                "Region -",
                "region",
            ),
        StatisticalSubjects.ELECTIONS to
            setOf(
                "Valg",
                "Stortingsvalg",
                "Fylkestings- og kommunestyrevalg",
            ),
        StatisticalSubjects.CULTURE_AND_RECREATION to
            setOf(
                "Tidsbruk",
                "Fritidsvirksomhet, kulturell tjenesteyting og sport",
                "Medlemskap i organisasjoner, tros- og livssynssamfunn",
            ),
        StatisticalSubjects.NATURE_AND_THE_ENVIRONMENT to
            setOf(
                "Arealbruk",
                "Luft",
                "Vann",
                "Avfall",
                "Miljøvernkostnader",
                "Miljøregnskap",
            ),
        StatisticalSubjects.ENERGY_AND_MANUFACTURING to
            setOf(
                "Energi",
                "Ordretilgang, ordrereserver",
                "Omsetning og produksjon",
                "Investering",
                "Produksjonsindekser",
                "Utvinning av råolje og naturgass",
                "Produksjon av nærings- og nytelsesmidler",
                "Elektrisitets-, gass-, damp- og varmtvannsforsyning",
            ),
        StatisticalSubjects.HEALTH to
            setOf(
                "Helse, sosiale forhold og kriminalitet",
                "Helsetjenester",
                "Sykehustjenester",
                "Legetjenester",
                "Andre helsetjenester",
            ),
        StatisticalSubjects.SOCIAL_CONDITIONS_WELFARE_AND_CRIME to
            setOf(
                "Barnevern",
                "Sosiale tjenester, trygd og sosialhjelp",
                "Trygd og sosialhjelp",
                "Kriminalitet og rettsvesen",
            ),
        StatisticalSubjects.EDUCATION to
            setOf(
                "Utdanning",
                "Utdanningsnivå",
                "Utdanningsinstitusjoner",
                "Barnehager, førskoler",
                "Universitet og høgskoler",
                "Voksenopplæring og annen undervisning",
            ),
        StatisticalSubjects.INCOME_AND_CONSUMPTION to
            setOf(
                "Personlig økonomi og boforhold",
                "Inntekt, formue, skatt",
                "Inntekt",
                "Formue",
                "Skatt",
                "Forbruk",
            ),
        StatisticalSubjects.CONSTRUCTION_HOUSING_AND_PROPERTY to
            setOf(
                "Bolig, boforhold",
                "Bygge- og anleggsvirksomhet",
                "Omsetning og drift av fast eiendom",
            ),
        StatisticalSubjects.LABOUR_MARKET_AND_EARNINGS to
            setOf(
                "Arbeidsliv, yrkesdeltaking og lønn",
                "Yrkesdeltaking",
                "Arbeidsmiljø, sykefravær",
                "Arbeidsledighet",
                "Arbeidskonflikter",
                "Lønn, arbeidskraftkostnader",
            ),
        StatisticalSubjects.PRICES_AND_PRICE_INDICES to
            setOf(
                "Priser, prisindekser og konjunkturindikatorer",
                "Pris- og kostnadsindekser, prisnivåsammenligninger",
                "Konsumprisindeks",
                "Produsentprisindekser - varer og tjenester",
                "Boligprisindekser",
            ),
        StatisticalSubjects.NATIONAL_ACCOUNTS_AND_BUSINESS_CYCLES to
            setOf(
                "Nasjonalregnskap og utenrikshandel",
                "Nasjonalregnskap",
                "Finansielle sektorbalanser",
            ),
        StatisticalSubjects.EXTERNAL_ECONOMY to
            setOf(
                "Utenriksregnskap",
                "Fordringer og gjeld overfor utlandet, direkteinvesteringer, SIFON",
                "Utenrikshandel",
            ),
        StatisticalSubjects.ESTABLISHMENTS_ENTERPRISES_AND_ACCOUNTS to
            setOf(
                "Næringsvirksomhet",
                "Struktur, bedriftregister",
                "Skatt næringsvirksomhet",
            ),
        StatisticalSubjects.TECHNOLOGY_AND_INNOVATION to
            setOf(
                "Teknologiske indikatorer, inkl. IKT",
            ),
        StatisticalSubjects.AGRICULTURE_FORESTRY_HUNTING_AND_FISHING to
            setOf(
                "Jordbruk, jakt, viltstell",
                "Jordbruk og skogbruk",
                "Skogbruk",
                "Fiske og fiskeoppdrett",
            ),
        StatisticalSubjects.WHOLESALE_AND_RETAIL_TRADE_AND_SERVICE_ACTIVITIES to
            setOf(
                "Varehandel",
            ),
        StatisticalSubjects.TRANSPORT_AND_TOURISM to
            setOf(
                "Hotell- og restaurantvirksomhet",
                "Transport og kommunikasjon",
                "Veitransport",
                "Sjøtransport",
                "Lufttransport",
                "Tjenester tilknyttet transport og reisevirksomhet",
            ),
        StatisticalSubjects.BANKING_AND_FINANCIAL_MARKETS to
            setOf(
                "Finansielle foretak",
                "Finansinstitusjoner",
                "Andre finansieller foretak",
                "Fonds- og aksjemegling, aksje- og obligasjonsfond, børser, legater",
                "Aksjer, obligasjoner, seritfikater og øvrig kredittmarked",
            ),
        StatisticalSubjects.PUBLIC_SECTOR to
            setOf(
                "Offentlige finanser",
                "Kommunale finanser",
            ),
    )

fun convertSubjectArea(name: String): StatisticalSubjects? =
    SubjectAreaMapping.entries
        .firstOrNull { entry -> name in entry.value }
        ?.key

fun specialSubjectFieldsMapping(vardokId: String): List<StatisticalSubjects>? =
    when (vardokId) {
        "2303", "2310" -> listOf(StatisticalSubjects.BANKING_AND_FINANCIAL_MARKETS)
        "3380", "3397" -> listOf(StatisticalSubjects.ENERGY_AND_MANUFACTURING)
        else -> null
    }
