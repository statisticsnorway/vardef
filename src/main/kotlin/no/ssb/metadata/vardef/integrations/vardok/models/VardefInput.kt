package no.ssb.metadata.vardef.integrations.vardok.models

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import no.ssb.metadata.vardef.models.Contact
import no.ssb.metadata.vardef.models.LanguageStringType
import java.net.URL

data class VardefInput(
    val name: LanguageStringType?,
    val shortName: String?,
    val definition: LanguageStringType?,
    var validFrom: String,
    val validUntil: String?,
    val unitTypes: List<String>,
    val externalReferenceUri: URL?,
    val comment: LanguageStringType?,
    val containsSpecialCategoriesOfPersonalData: Boolean,
    val subjectFields: List<String>,
    val classificationReference: String?,
    val contact: Contact,
    val measurementType: String?,
    val relatedVariableDefinitionUris: List<String>?,
) {
    override fun toString(): String {
        val mapper = ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        return mapper.writeValueAsString(this)
    }
}
