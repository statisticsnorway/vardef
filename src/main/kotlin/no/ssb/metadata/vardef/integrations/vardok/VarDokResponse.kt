package no.ssb.metadata.vardef.integrations.vardok

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import io.micronaut.core.annotation.Introspected
import io.micronaut.serde.annotation.Serdeable

@Serdeable
@Introspected
data class DC(
    @field:JacksonXmlProperty(localName = "contributor")
    val contributor: String?,
    @field:JacksonXmlProperty(localName = "creator")
    val creator: String?,
    @field:JacksonXmlProperty(localName = "modified")
    val modified: String?,
    @field:JacksonXmlProperty(localName = "valid")
    val valid: String?,
    val description: String?,
    @field:JacksonXmlProperty(localName = "abstract")
    val abstractText: String?,
    @field:JacksonXmlProperty(localName = "tableOfContents")
    val tableOfContents: String?,
    val format: String?,
    @field:JacksonXmlProperty(localName = "identifier")
    val identifier: String?,
    val language: String?,
    val publisher: String?,
    val rights: String?,
    val source: String?,
    val subject: String?,
    @field:JacksonXmlProperty(localName = "title")
    val title: String?,
    @field:JacksonXmlProperty(localName = "type")
    val type: String?,
)

@Serdeable
@Introspected
data class ContactPerson(
    val codeValue: String,
    val codeText: String,
)

@Serdeable
@Introspected
data class ContactDivision(
    val codeValue: String,
    val codeText: String,
)

@Serdeable
@Introspected
data class Common(
    @JacksonXmlProperty(localName = "Title", namespace = "xml:lang")
    val title: String,
    @field:JacksonXmlProperty(localName = "Description")
    val description: String?,
    val contactPerson: ContactPerson?,
    val contactDivision: ContactDivision?,
    val notes: String? = null,
)

@Serdeable
@Introspected
data class SubjectArea(
    val codeValue: String?,
    val codeText: String?,
)

@Serdeable
@Introspected
data class ShortNameWeb(
    @field:JacksonXmlProperty(localName = "CodeValue")
    val codeValue: String? = null,
    @field:JacksonXmlProperty(localName = "CodeText")
    val codeText: String? = null,
)

@Serdeable
@Introspected
data class Variable(
    @field:JacksonXmlProperty(localName = "InternalNotes")
    val internalNotes: String? = null,
    @field:JacksonXmlProperty(localName = "StatisticalUnit")
    val statisticalUnit: String?,
    @field:JacksonXmlProperty(localName = "SubjectArea")
    val subjectArea: SubjectArea?,
    @field:JacksonXmlProperty(localName = "ExternalSource")
    val externalSource: String? = null,
    @field:JacksonXmlProperty(localName = "InternalSource")
    val internalSource: String? = null,
    @field:JacksonXmlProperty(localName = "Sensitivity")
    val sensitivity: String,
    val externalDocument: String? = null,
    val dataElementName: String? = null,
    @field:JacksonXmlProperty(localName = "ShortNameWeb", isAttribute = false)
    val shortNameWeb: ShortNameWeb?,
    val calculation: String? = null,
    val internalDocument: String? = null,
    val externalComment: String? = null,
    val internalReference: String? = null,
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
    @field:JacksonXmlProperty(isAttribute = true, localName = "lang")
    val xmlLang: String,
    @field:JacksonXmlProperty(isAttribute = true, localName = "schemaLocation")
    val xsiSchemaLocation: String,
    @field:JacksonXmlProperty(localName = "DC", isAttribute = false)
    val dc: DC?,
    @field:JacksonXmlProperty(localName = "Common", isAttribute = false)
    val common: Common?,
    @field:JacksonXmlProperty(localName = "Variable", isAttribute = false)
    val variable: Variable?,
    val relations: String? = null,
)

