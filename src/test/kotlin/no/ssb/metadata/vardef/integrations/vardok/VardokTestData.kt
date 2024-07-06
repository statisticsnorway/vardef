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
