package no.ssb.metadata.vardef.integrations.vardok

val validFromDateAndEnInOtherLanguages1466 =
    """
    <FIMD xmlns="http://www.ssb.no/ns/fimd" xmlns:dc="http://purl.org/dc/elements/1.1/" 
    xmlns:dcterms="http//purl.org/dc/terms/" xmlns:xlink="http://www.w3.org/1999/xlink" 
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instanse" createdOn="2005-12-12" defaultValidFrom="2003-11-18" 
    defaultValidTo="" id="urn:ssb:conceptvariable:vardok:1466" lastChangedDate="2009-05-27" otherLanguages="en" 
    type="ConceptVariable" xml:lang="nb" xsi:schemaLocation="http://www.ssb.no/ns/fimd Q:\DOK\FIMD\IT-analyse\Schemas\fimdv3.xsd">
        <DC>
            <dc:contributor>Seksjon for statistiske populasjoner</dc:contributor>
            <dc:creator>Liv Reidun Sletmoen</dc:creator>
            <dcterms:modified>2009-05-27</dcterms:modified>
            <dcterms:valid>2003-11-18 - </dcterms:valid>
            <dc:description>Registreringsdatoen til en variabel er den datoen en endring eller korreksjon er registrert 
            i BoF-basen.</dc:description>
            <dcterms:abstract> Det sentrale Bedrifts- og foretaksregister</dcterms:abstract>
            <dcterms:tableOfContents>Foretak</dcterms:tableOfContents>
            <dc:format>text/xml</dc:format>
            <dc:identifier>urn:ssb:conceptvariable:vardok:1466</dc:identifier>
            <dc:language>nb</dc:language>
            <dc:publisher>Statistisk sentralbyrï¿½</dc:publisher>
            <dc:rights>Statistiske metadata, produsert av Statistisk sentralbyrï¿½ (SSB) til produksjon av statistikk 
            og annen alminnelig bruk.</dc:rights>
            <dc:source/>
            <dc:subject>10.01 - Struktur, bedriftregister</dc:subject>
            <dc:title>Registreringsdato</dc:title>
            <dc:type>Variabeldefinisjon</dc:type>
        </DC>
        <Common>
            <Title>Registreringsdato</Title>
            <Description>Registreringsdatoen til en variabel er den datoen en endring eller korreksjon er registrert 
            i BoF-basen.</Description>
            <ContactPerson>
                <CodeValue>lrs</CodeValue>
                <CodeText>Liv Reidun Sletmoen</CodeText>
            </ContactPerson>
            <ContactDivision>
                <CodeValue>810</CodeValue>
                <CodeText>Seksjon for statistiske populasjoner</CodeText>
            </ContactDivision>
            <Notes>Denne definisjonen gjelder både for bedrift og foretak.</Notes>
        </Common>
        <Variable>
            <InternalNotes/>
            <StatisticalUnit>Foretak</StatisticalUnit>
            <SubjectArea>
                <CodeValue>10.01</CodeValue>
                <CodeText>Struktur, bedriftregister</CodeText>
            </SubjectArea>
            <ExternalSource/>
            <InternalSource>Det sentrale Bedrifts- og foretaksregister</InternalSource>
            <Sensitivity>Ordinï¿½r</Sensitivity>
            <ExternalDocument/>
            <DataElementName>r_dato</DataElementName>
            <ShortNameWeb/>
            <Calculation/>
            <InternalDocument/>
            <ExternalComment/>
            <InternalReference/>
        </Variable>
        <Relations/>
    </FIMD>
    """.trimIndent()

val vardokResponseOk =
    VardokResponse(
        createdOn = "2002-07-12",
        defaultValidFrom = "1993-01-01",
        defaultValidTo = null,
        id = "urn:ssb:conceptvariable:vardok:2",
        lastChangedDate = "2009-01-14",
        otherLanguages = "en",
        type = "ConceptVariable",
        xmlLang = "nb",
        xsiSchemaLocation = "http://www.ssb.no/ns/fimd Q:DOKFIMDIT-analyseSchemasfimdv3.xsd",
        dc =
            DC(
                contributor = "Seksjon for inntekts- og lønnsstatistikk",
                creator = "Vidar Pedersen",
                modified = "2009-01-14",
                valid = "1993-01-01 - ",
                description =
                    "Yrkesinntekter, kapitalinntekter, skattepliktige og skattefrie overføringer, i " +
                        "løpet av kalenderåret. Utlignet skatt og negative overføringer er trukket i fra.",
                abstract = "Inntektsstatistikk for husholdninger",
                tableOfContents = "Person",
                format = "text/xml",
                identifier = "urn:ssb:conceptvariable:vardok:2",
                language = "nb",
                publisher = "Statistisk sentralbyrï¿½",
                rights =
                    "Statistiske metadata, produsert av Statistisk sentralbyrï¿½ (SSB) til produksjon av " +
                        "statistikk og annen alminnelig bruk.",
                source = null,
                subject = "05.01 - Inntekt, formue, skatt",
                title = "Inntekt etter skatt",
                type = "Variabeldefinisjon",
            ),
        common =
            Common(
                title = "Inntekt etter skatt",
                description =
                    "Yrkesinntekter, kapitalinntekter, skattepliktige og skattefrie overføringer, i løpet " +
                        "av kalenderåret. Utlignet skatt og negative overføringer er trukket i fra.",
                contactPerson = null,
                contactDivision =
                    ContactDivision(
                        codeValue = "225",
                        codeText = "Seksjon for inntekts- og lønnsstatistikk",
                    ),
                notes = null,
            ),
        variable =
            Variable(
                internalNotes = null,
                statisticalUnit = "Person",
                subjectArea =
                    SubjectArea(
                        codeValue = "05.01",
                        codeText = "Inntekt, formue, skatt",
                    ),
                externalSource = null,
                internalSource = "Inntektsstatistikk for husholdninger",
                sensitivity = "Ordinï¿½r",
                externalDocument = null,
                dataElementName = "wies",
                // shortNameWeb = null,
                calculation = null,
                internalDocument = null,
                externalComment = null,
                internalReference = null,
            ),
        relations = null,
    )
