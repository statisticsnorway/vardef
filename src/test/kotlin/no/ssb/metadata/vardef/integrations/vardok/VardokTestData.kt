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


val validUntilDate49 = """
    <FIMD xmlns="http://www.ssb.no/ns/fimd" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dcterms="http//purl.org/dc/terms/" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instanse" createdOn="2002-09-10" defaultValidFrom="1993-01-01" defaultValidTo="2005-12-31" id="urn:ssb:conceptvariable:vardok:49" lastChangedDate="2016-01-04" otherLanguages="en" type="ConceptVariable" xml:lang="nb" xsi:schemaLocation="http://www.ssb.no/ns/fimd Q:\DOK\FIMD\IT-analyse\Schemas\fimdv3.xsd">
    <DC>
    <dc:contributor>Seksjon for inntekts- og lønnsstatistikk</dc:contributor>
    <dc:creator>Vidar Pedersen</dc:creator>
    <dcterms:modified>2016-01-04</dcterms:modified>
    <dcterms:valid>1993-01-01 - 2005-12-31</dcterms:valid>
    <dc:description>Yrkesinntekter er summen av lønnsinntekter og netto næringsinntekter i løpet av kalenderåret. Sykepenger og fødselspenger er inkludert.</dc:description>
    <dcterms:abstract> Inntektsstatistikk for husholdninger</dcterms:abstract>
    <dcterms:tableOfContents>Person</dcterms:tableOfContents>
    <dc:format>text/xml</dc:format>
    <dc:identifier>urn:ssb:conceptvariable:vardok:49</dc:identifier>
    <dc:language>nb</dc:language>
    <dc:publisher>Statistisk sentralbyrï¿½</dc:publisher>
    <dc:rights>Statistiske metadata, produsert av Statistisk sentralbyrï¿½ (SSB) til produksjon av statistikk og annen alminnelig bruk.</dc:rights>
    <dc:source/>
    <dc:subject>05.01 - Inntekt, formue, skatt</dc:subject>
    <dc:title>Yrkesinntekter</dc:title>
    <dc:type>Variabeldefinisjon</dc:type>
    </DC>
    <Common>
    <Title>Yrkesinntekter</Title>
    <Description>Yrkesinntekter er summen av lønnsinntekter og netto næringsinntekter i løpet av kalenderåret. Sykepenger og fødselspenger er inkludert.</Description>
    <ContactPerson>
    <CodeValue>vip</CodeValue>
    <CodeText>Vidar Pedersen</CodeText>
    </ContactPerson>
    <ContactDivision>
    <CodeValue>225</CodeValue>
    <CodeText>Seksjon for inntekts- og lønnsstatistikk</CodeText>
    </ContactDivision>
    <Notes>Fra og med inntektsåret 2006 trekkes sykepenger og fødselspenger fra og legges til skattepliktige overføringer i den offisielle statistikken.</Notes>
    </Common>
    <Variable>
    <InternalNotes/>
    <StatisticalUnit>Person</StatisticalUnit>
    <SubjectArea>
    <CodeValue>05.01</CodeValue>
    <CodeText>Inntekt, formue, skatt</CodeText>
    </SubjectArea>
    <ExternalSource/>
    <InternalSource>Inntektsstatistikk for husholdninger</InternalSource>
    <Sensitivity>Ordinï¿½r</Sensitivity>
    <ExternalDocument>http://www.ssb.no/emner/05/90/notat_200372/notat_200372.pdf</ExternalDocument>
    <DataElementName>wyrkinnt</DataElementName>
    <ShortNameWeb>
    <CodeValue>ifhus</CodeValue>
    <CodeText>Inntektsstatistikk for husholdninger. Husholdningstyper</CodeText>
    </ShortNameWeb>
    <Calculation/>
    <InternalDocument/>
    <ExternalComment/>
    <InternalReference/>
    </Variable>
    <Relations>
    <ConceptVariableRelation xlink:href="http://www.ssb.no/conceptvariable/vardok/3318" xlink:type="simple"/>
    <ConceptVariableRelation xlink:href="http://www.ssb.no/conceptvariable/vardok/15" xlink:type="simple"/>
    <ConceptVariableRelation xlink:href="http://www.ssb.no/conceptvariable/vardok/13" xlink:type="simple"/>
    <ConceptVariableVersion xlink:href="http://www.ssb.no/conceptvariable/vardok/3318" xlink:type="simple"/>
    <StatbankTableRelation xlink:href="http://www.ssb.no/tabell/06961" xlink:title="06961: Inntektsindikatorer for ulike grupper (prosent) (avslutta serie)" xlink:type="simple"/>
    <StatbankTableRelation xlink:href="http://www.ssb.no/tabell/06467" xlink:title="06467: Inntektsrekneskap for hushald, etter hushaldstype (avslutta serie)" xlink:type="simple"/>
    <StatbankTableRelation xlink:href="http://www.ssb.no/tabell/06120" xlink:title="06120: Hovedposter i inntektsregnskapet for bosatte uførepensjonister (avslutta serie)" xlink:type="simple"/>
    <StatbankTableRelation xlink:href="http://www.ssb.no/tabell/04619" xlink:title="04619: Inntektsregnskap for bosatte personer (avslutta serie)" xlink:type="simple"/>
    </Relations>
    </FIMD>
""".trimIndent()