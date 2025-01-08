package no.ssb.metadata.vardef.integrations.vardok.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import io.micronaut.core.annotation.Introspected
import io.micronaut.serde.annotation.Serdeable

@Serdeable
@Introspected
data class DC(
    val contributor: String?,
    val creator: String?,
    val modified: String?,
    val valid: String?,
    val description: String?,
    val abstract: String?,
    val tableOfContents: String?,
    val format: String?,
    val identifier: String?,
    val language: String?,
    val publisher: String?,
    val rights: String?,
    val source: String?,
    val subject: String?,
    val title: String?,
    val type: String?,
)

@Serdeable
@Introspected
data class ContactPerson(
    @field:JacksonXmlProperty(localName = "CodeValue")
    val codeValue: String?,
    @field:JacksonXmlProperty(localName = "CodeText")
    val codeText: String?,
)

@Serdeable
@Introspected
data class ContactDivision(
    @field:JacksonXmlProperty(localName = "CodeValue")
    val codeValue: String,
    @field:JacksonXmlProperty(localName = "CodeText")
    val codeText: String,
)

@Serdeable
@Introspected
data class Common(
    @field:JacksonXmlProperty(localName = "Title", namespace = "xml:lang")
    val title: String,
    @field:JacksonXmlProperty(localName = "Description")
    val description: String?,
    @field:JacksonXmlProperty(localName = "ContactPerson")
    val contactPerson: ContactPerson?,
    @field:JacksonXmlProperty(localName = "ContactDivision")
    val contactDivision: ContactDivision,
    @field:JacksonXmlProperty(localName = "Notes")
    val notes: String? = null,
)

@Serdeable
@Introspected
data class SubjectArea(
    @field:JacksonXmlProperty(localName = "CodeValue")
    val codeValue: String?,
    @field:JacksonXmlProperty(localName = "CodeText")
    val codeText: String?,
)

@Serdeable
@Introspected
@JsonIgnoreProperties(ignoreUnknown = true)
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
    @field:JacksonXmlProperty(localName = "ExternalDocument")
    val externalDocument: String? = null,
    @field:JacksonXmlProperty(localName = "DataElementName")
    val dataElementName: String? = null,
    @field:JacksonXmlProperty(localName = "Calculation")
    val calculation: String? = null,
    val internalDocument: String? = null,
    val externalComment: String? = null,
    val internalReference: String? = null,
)

@Serdeable
@Introspected
@JacksonXmlRootElement(localName = "FIMD")
@JsonIgnoreProperties(ignoreUnknown = true)
data class VardokResponse(
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
    val variable: Variable? = null,
    val relations: String? = null,
)
