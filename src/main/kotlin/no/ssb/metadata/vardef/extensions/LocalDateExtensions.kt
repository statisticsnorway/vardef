package no.ssb.metadata.vardef.extensions

import java.time.LocalDate

fun LocalDate.isEqualOrAfter(other: LocalDate) = this.isEqual(other) || this.isAfter(other)

fun LocalDate.isEqualOrBefore(other: LocalDate) = this.isEqual(other) || this.isBefore(other)