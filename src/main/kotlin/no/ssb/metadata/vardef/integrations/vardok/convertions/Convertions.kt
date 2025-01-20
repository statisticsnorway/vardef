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

/**
 * When null response from method [convertUnitTypes] identifier is checked in method [specialCaseUnitMapping]
 *
 * @returns list except if result is null then
 * @throws OutdatedUnitTypesException
 */
fun mapVardokStatisticalUnitToUnitTypes(vardokItem: VardokResponse): List<String?> =
    vardokItem.variable?.statisticalUnit?.let { statUnit ->
        convertUnitTypes(statUnit) ?: specialCaseUnitMapping(vardokItem.id.substringAfterLast(":"))
    } ?: specialCaseUnitMapping(vardokItem.id)
        ?: throw OutdatedUnitTypesException(vardokItem.id.substringAfterLast(":"))

fun mapVardokSubjectAreaToSubjectFiled(vardokItem: VardokResponse): List<String?> =
    vardokItem.variable?.subjectArea?.codeText?.let {
        listOf(convertSubjectArea(it))
    } ?: throw OutdatedSubjectAreaException(vardokItem.id.substringAfterLast(":"))

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
