package no.ssb.metadata.vardef.integrations.vardok.convertions

import no.ssb.metadata.vardef.integrations.vardok.models.OutdatedSubjectAreaException
import no.ssb.metadata.vardef.integrations.vardok.models.StatisticalUnitException
import no.ssb.metadata.vardef.integrations.vardok.models.VardokResponse
import no.ssb.metadata.vardef.models.SupportedLanguages
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.URL

private val logger = LoggerFactory.getLogger("Convertions")

fun getValidDates(vardokItem: VardokResponse): Pair<String, String?> {
    val dateString = vardokItem.dc?.valid?.split(" - ")

    var firstDate = dateString?.getOrNull(0)?.trim()?.takeIf { it.isNotEmpty() }
    val secondDate = dateString?.getOrNull(1)?.trim()?.takeIf { it.isNotEmpty() }

    if (firstDate == null) {
        firstDate = "1900-01-01"
    }
    return Pair(firstDate, secondDate)
}

/**
 * Maps a *StatisticalUnit* field to a valid Klass code
 *
 * This function converts a *StatisticalUnit* text field to a valid Klass code from codelist 702.
 * For some special cases, the mapping is determined by the ID rather than the *StatisticalUnit* field.
 *
 * The mapping follows [Migreringsplan fra Vardok til Vardef](https://statistics-norway.atlassian.net/wiki/spaces/DAPLA/pages/4128276501/Migreringsplan+fra+Vardok+til+Vardef#Enhetstyper)
 * and includes identifiers handled in [specialCaseUnitMapping]
 *
 * @returns list with string values representing the mapped Klass codes
 * If result is null then neither [specialCaseUnitMapping] nor [convertUnitTypes] produced a valid code
 * @throws StatisticalUnitException if mapping fails due to a missing or unrecognized *StatisticalUnit*.
 *
 */
fun mapVardokStatisticalUnitToUnitTypes(vardokItem: VardokResponse): List<String> {
    specialCaseUnitMapping(vardokItem.parseId())?.let { return it }

    return vardokItem.variable?.statisticalUnit?.let { statUnit ->
        convertUnitTypes(statUnit)
    } ?: throw StatisticalUnitException(vardokItem.parseId())
}

/**
 *
 * @returns list except if result is null then
 * @throws OutdatedSubjectAreaException
 */
fun mapVardokSubjectAreaToSubjectFiled(vardokItem: VardokResponse): List<String> {
    specialSubjectFieldsMapping(vardokItem.parseId())?.let { return it }

    val code =
        vardokItem.variable?.subjectArea?.codeText
            ?: return emptyList()

    return convertSubjectArea(code)?.let { listOf(it) }
        ?: throw OutdatedSubjectAreaException(vardokItem.parseId(), code)
}

/**
 * Maps the `notes` and `calculation` fields of a `VardokItem` to a `LanguageStringType` object.
 *
 * This function processes the `common.notes` and `variable.calculation` fields in the `VardokResponse` object
 * for each language (`nb`, `nn`, `en`) based on the following rules:
 *
 * - If both `notes` and `calculation` are empty, it assigns `null`.
 * - If `notes` is empty and `calculation` is not empty, it assigns the value of `calculation`.
 * - If `calculation` is empty and `notes` is not empty, it assigns the value of `notes`.
 * - If both `notes` and `calculation` are non-empty, it concatenates them and assigns the result.
 *
 * The function preserves existing values for each language while processing only the relevant fields.
 *
 * @param vardokItem A map where the keys are language codes (`nb`, `nn`, `en`) and the values are `VardokResponse` objects.
 * @return A `LanguageStringType` object containing the mapped comment strings for each language.
 */
fun mapVardokComment(vardokItem: Map<String, VardokResponse>): MutableMap<String, String?> {
    val languageComments = mutableMapOf<String, String?>()
    for (language in SupportedLanguages.entries) {
        val notes = vardokItem[language.toString()]?.common?.notes
        val calculation = vardokItem[language.toString()]?.variable?.calculation

        languageComments[language.toString()] =
            when {
                notes.isNullOrEmpty() && calculation.isNullOrEmpty() -> null
                notes.isNullOrEmpty() -> calculation
                calculation.isNullOrEmpty() -> notes
                else -> notes + calculation
            }
    }
    return languageComments
}

/**
 * Converts an external document URL from a [VardokResponse] into a [URL] if it is valid.
 *
 * The function extracts the `externalDocument` field from the given [vardokItem], which is a string.
 * If valid, it converts the string into a [URL] object, ensuring that the resulting URL has a valid host.
 *
 * @param vardokItem The [VardokResponse] containing the external document URL as a string.
 * @return A valid [URL] if the `externalDocument` string is properly formatted, otherwise `null`.
 */
fun mapExternalDocumentToUri(vardokItem: VardokResponse): URL? {
    logger.info("Convert external document value: ${vardokItem.variable?.externalDocument} for ${vardokItem.parseId()}.")
    return vardokItem.variable
        ?.externalDocument
        ?.trim()
        ?.let { urlString ->
            runCatching { URI(urlString).toURL() }
                .onFailure { logger.error("Invalid URL: $urlString - Error: ${it.message}") }
                .getOrNull()
        }
}

/**
 * Extracts a list of URL strings from the `conceptVariableRelations` of a `VardokResponse` object.
 *
 * @param vardokItem The `VardokResponse` object containing the relations data.
 * @return A list of non-null `href` strings, or `null` if `conceptVariableRelations` is null.
 */
fun mapConceptVariableRelations(vardokItem: VardokResponse): List<String>? =
    vardokItem.relations
        ?.conceptVariableRelations
        ?.mapNotNull { it?.href }
