package no.ssb.metadata.vardef.utils

import no.ssb.metadata.vardef.models.LanguageStringType
import java.time.LocalDate

class ServiceUtils {
    companion object {
        /**
         * Checks whether the given object is not null or empty.
         *
         *  This function acts as an extension for various types:
         *  - **String**: Returns `true` if the string is not blank.
         *  - **Collection**: Returns `true` if the collection is not empty and contains at least one
         *    non-null/non-empty element.
         *  - **Map**: Returns `true` if the map is not empty.
         *  - **Any other type**: Returns `true` if the object is not null.
         *
         *  @return `true` if the object is not null or empty, `false` otherwise.
         */
        fun Any?.isNotNullOrEmpty(): Boolean =
            when (this) {
                is String -> this.isNotBlank()
                is Collection<*> -> this.isNotEmpty() && this.any { it.isNotNullOrEmpty() }
                else -> this != null
            }

        /**
         * Checks whether all language entries in the `LanguageStringType` are not null or empty.
         *
         * @return `true` if all language entries are not null or empty, `false` otherwise.
         */
        fun LanguageStringType.isNotNullOrEmptyAllLanguages(): Boolean = this.listPresentLanguages().all { it.isNotNullOrEmpty() }

        /**
         * Checks if the given dates are in the correct chronological order.
         *
         * @param validFrom The starting date (may be `null`).
         * @param validUntil The ending date (may be `null`).
         * @return `true` if the dates are in the correct order, or if either date is `null`.
         *         Otherwise, `false` if `validFrom` occurs after `validUntil`.
         */
        fun isCorrectDateOrder(
            validFrom: LocalDate?,
            validUntil: LocalDate?,
        ): Boolean = validFrom == null || validUntil == null || validFrom.isBefore(validUntil)
    }
}
