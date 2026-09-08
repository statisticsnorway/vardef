package no.ssb.metadata.vardef.config

import io.micronaut.context.annotation.EachProperty
import io.micronaut.context.annotation.Parameter
import jakarta.inject.Singleton
import java.time.LocalDate
import java.time.format.DateTimeParseException

@Singleton
class KlassConfiguration(
    private val classifications: List<KlassClassificationConfiguration>,
) {
    fun subjectFieldsId(): String =
        classificationByName(SUBJECT_FIELDS_NAME)?.id
            ?: throw IllegalStateException("Missing klass.classifications.subject-fields.id")

    fun unitTypesId(): String =
        classificationByName(UNIT_TYPES_NAME)?.id
            ?: throw IllegalStateException("Missing klass.classifications.unit-types.id")

    fun measurementTypeId(): String =
        classificationByName(MEASUREMENT_TYPE_NAME)?.id
            ?: throw IllegalStateException("Missing klass.classifications.measurement-type.id")

    fun measurementTypeLevel(): Int =
        classificationByName(MEASUREMENT_TYPE_NAME)?.levels?.firstOrNull()
            ?: throw IllegalStateException("Missing klass.classifications.measurement-type.levels[0]")

    fun codesAtForClassification(classificationId: String): String {
        val configuredClassification = classifications.firstOrNull { it.id == classificationId }

        if (configuredClassification == null) {
            return LocalDate.now().toString()
        }

        val configuredDate = configuredClassification.date ?: TODAY

        return resolveCodesAt(configuredDate)
    }

    private fun classificationByName(name: String): KlassClassificationConfiguration? = classifications.firstOrNull { it.name == name }

    private fun resolveCodesAt(value: String): String =
        if (value.equals(TODAY, ignoreCase = true)) {
            LocalDate.now().toString()
        } else {
            try {
                LocalDate.parse(value).toString()
            } catch (_: DateTimeParseException) {
                throw IllegalStateException("Invalid klass date '$value'. Expected 'today' or YYYY-MM-DD")
            }
        }

    companion object {
        private const val TODAY = "today"
        private const val SUBJECT_FIELDS_NAME = "subject-fields"
        private const val UNIT_TYPES_NAME = "unit-types"
        private const val MEASUREMENT_TYPE_NAME = "measurement-type"
    }
}

@EachProperty("klass.classifications")
class KlassClassificationConfiguration(
    @param:Parameter val name: String,
) {
    var id: String = ""
    var date: String? = null
    var levels: List<Int>? = null
}
