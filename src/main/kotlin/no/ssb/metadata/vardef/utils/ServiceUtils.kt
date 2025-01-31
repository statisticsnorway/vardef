package no.ssb.metadata.vardef.utils

import java.time.LocalDate

class ServiceUtils {
    companion object {

        /**
         *
         */
        fun Any?.isNotNullOrEmpty(): Boolean =
            when (this) {
                is String -> this.isNotBlank() // String: Not null and not blank
                is Collection<*> -> this.isNotEmpty() && this.any { it.isNotNullOrEmpty() } // Lists/Sets: Not empty
                is Map<*, *> -> this.isNotEmpty() // Maps: Not empty
                else -> this != null // Any other type: Just not null
            }

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