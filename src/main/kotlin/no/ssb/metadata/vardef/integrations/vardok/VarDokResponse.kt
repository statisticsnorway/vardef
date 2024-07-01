package no.ssb.metadata.vardef.integrations.vardok

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import io.micronaut.core.annotation.Introspected

import io.micronaut.serde.annotation.Serdeable

@Introspected
@JacksonXmlRootElement(localName = "vardok-response")
data class VarDokResponse( @JacksonXmlProperty(localName = "title") val name: String?)


@Serdeable
@Introspected
data class DC(
    @field:JacksonXmlProperty(localName = "contributor", namespace = "http://purl.org/dc/elements/1.1/")
    val contributor: String?,
    @field:JacksonXmlProperty(localName = "creator", namespace = "http://purl.org/dc/elements/1.1/")
    val creator: String?,
    @field:JacksonXmlProperty(localName = "modified", namespace = "http://purl.org/dc/terms/")
    val modified: String?,
    @field:JacksonXmlProperty(localName = "valid", namespace = "http://purl.org/dc/terms/")
    val valid: String?,
    val description: String?,
    @field:JacksonXmlProperty(localName = "abstract", namespace = "http://purl.org/dc/terms/")
    val abstractText: String?,
    @field:JacksonXmlProperty(localName = "tableOfContents", namespace = "http://purl.org/dc/terms/")
    val tableOfContents: String?,
    val format: String?,
    @field:JacksonXmlProperty(localName = "identifier", namespace = "http://purl.org/dc/elements/1.1/")
    val identifier: String?,
    val language: String?,
    val publisher: String?,
    val rights: String?,
    val source: String?,
    val subject: String?,
    /*@field:JacksonXmlProperty(localName = "title", namespace = "http://purl.org/dc/elements/1.1/")
    val title: String?,
    @field:JacksonXmlProperty(localName = "type", namespace = "http://purl.org/dc/elements/1.1/")
    val type: String?
    val creator: String?,
    val modified: String?,
    val valid: String?,
    val description: String?,
    val tableOfContents: String?,
    val format: String?,
    val identifier: String?,
    val language: String?,
    val publisher: String?,
    val rights: String?,
    val source: String? = null,
    val subject: String?,
    @JacksonXmlProperty(localName = "dc:title", isAttribute = false)
    val title: String,
    val type: String?,*/
)

@Serdeable
@Introspected
data class ContactPerson(
    val codeValue: String,
    val codeText: String
)

@Serdeable
@Introspected
data class ContactDivision(
    val codeValue: String,
    val codeText: String
)

@Serdeable
@Introspected
data class Common(
    @JacksonXmlProperty(localName = "Title")
    val title: String,
    @field:JacksonXmlProperty(localName = "Description")
    val description: String?,
    val contactPerson: ContactPerson?,
    val contactDivision: ContactDivision?,
    val notes: String? = null
)

@Serdeable
@Introspected
data class SubjectArea(
    val codeValue: String,
    val codeText: String
)

@Serdeable
@Introspected
data class ShortNameWeb(
    val codeValue: String,
    val codeText: String
)

@Serdeable
@Introspected
data class Variable(
    val internalNotes: String? = null,
    val statisticalUnit: String,
    val subjectArea: SubjectArea,
    val externalSource: String? = null,
    val internalSource: String? = null,
    val sensitivity: String,
    val externalDocument: String? = null,
    val dataElementName: String? = null,
    val shortNameWeb: ShortNameWeb,
    val calculation: String? = null,
    val internalDocument: String? = null,
    val externalComment: String? = null,
    val internalReference: String? = null
)

@Serdeable
@Introspected
@JacksonXmlRootElement(localName = "fimd")
data class FIMD(

    val createdOn: String,
    val defaultValidFrom: String,
    val defaultValidTo: String? = null,
    val id: String,
    val lastChangedDate: String,
    val otherLanguages: String,
    val type: String,
    val xmlLang: String?,
    val xsiSchemaLocation: String?,
    @field:JacksonXmlProperty(localName = "DC", isAttribute = true)
    val dc: DC?,
    @field:JacksonXmlProperty(localName = "Common", isAttribute = true)
    val common: Common?,
    val variable: Variable?,
    val relations: String? = null
)

