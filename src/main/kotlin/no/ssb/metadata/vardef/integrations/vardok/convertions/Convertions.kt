package no.ssb.metadata.vardef.integrations.vardok.convertions

import no.ssb.metadata.vardef.integrations.vardok.models.OutdatedSubjectAreaException
import no.ssb.metadata.vardef.integrations.vardok.models.OutdatedUnitTypesException
import no.ssb.metadata.vardef.integrations.vardok.models.VardokResponse
import no.ssb.metadata.vardef.models.SupportedLanguages

fun getValidDates(vardokItem: VardokResponse): Pair<String, String?> {
    val dateString = vardokItem.dc?.valid?.split(" - ")

    var firstDate = dateString?.getOrNull(0)?.trim()?.takeIf { it.isNotEmpty() }
    val secondDate = dateString?.getOrNull(1)?.trim()?.takeIf { it.isNotEmpty() }

    if (firstDate == null) {
        firstDate = "1900-01-01"
    }
    return Pair(firstDate, secondDate)
}

fun vardokId(vardokItem: VardokResponse): String = vardokItem.id.substringAfterLast(":")

/**
 * When null response from method [convertUnitTypes] identifier is checked in method [specialCaseUnitMapping]
 *
 * @returns list with string value(s) except if result is null then
 * @throws OutdatedUnitTypesException
 */
fun mapVardokStatisticalUnitToUnitTypes(vardokItem: VardokResponse): List<String> =
    // Handle Vardok id 3125 which does not conform to neither 'converUnitTypes' nor 'specialCaseUnitMapping'
    if (vardokId(vardokItem) == "3125") {
        listOf("21")
    } else {
        vardokItem.variable?.statisticalUnit?.let { statUnit ->
            convertUnitTypes(statUnit) ?: specialCaseUnitMapping(vardokId(vardokItem))
        } ?: specialCaseUnitMapping(vardokId(vardokItem))
    } ?: throw OutdatedUnitTypesException(vardokId(vardokItem))

/**
 *
 * @returns list except if result is null then
 * @throws OutdatedSubjectAreaException
 */
fun mapVardokSubjectAreaToSubjectFiled(vardokItem: VardokResponse): List<String> {
    val code =
        vardokItem.variable?.subjectArea?.codeText
            ?: return emptyList()

    return convertSubjectArea(code)?.let { listOf(it) }
        ?: throw OutdatedSubjectAreaException(vardokItem.id.substringAfterLast(":"))
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
