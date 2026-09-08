package no.ssb.metadata.vardef.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

class KlassConfigurationTest {
    private fun classification(
        name: String,
        id: String,
        date: String? = null,
        levels: List<Int>? = null,
    ): KlassClassificationConfiguration =
        KlassClassificationConfiguration(name).apply {
            this.id = id
            this.date = date
            this.levels = levels
        }

    private fun configuration(
        classifications: List<KlassClassificationConfiguration>,
    ): KlassConfiguration = KlassConfiguration(classifications)

    private fun configuredClassifications(): List<KlassClassificationConfiguration> =
        listOf(
            classification("subject-fields", "618"),
            classification("unit-types", "702"),
            classification("measurement-type", "303", levels = listOf(1)),
        )

    @Test
    fun `codesAtForClassification resolves today keyword`() {
        val configuration =
            configuration(
                classifications = configuredClassifications(),
            )

        assertThat(configuration.codesAtForClassification("702")).isEqualTo(LocalDate.now().toString())
    }

    @Test
    fun `codesAtForClassification resolves explicit iso date on classification`() {
        val configuration =
            configuration(
                classifications =
                    listOf(
                        classification("subject-fields", "618", date = "today"),
                        classification("unit-types", "702", date = "2024-08-01"),
                        classification("measurement-type", "303", date = "today", levels = listOf(1)),
                    ),
            )

        assertThat(configuration.codesAtForClassification("702")).isEqualTo("2024-08-01")
    }

    @Test
    fun `codesAtForClassification prefers classification specific date`() {
        val configuration =
            configuration(
                classifications =
                    listOf(
                        classification("subject-fields", "618"),
                        classification("unit-types", "702"),
                        classification("measurement-type", "303", date = "2023-06-30", levels = listOf(1)),
                    ),
            )

        assertThat(configuration.codesAtForClassification("303")).isEqualTo("2023-06-30")
    }

    @Test
    fun `codesAtForClassification defaults to today for unknown classification id`() {
        val configuration =
            configuration(classifications = configuredClassifications())

        assertThat(configuration.codesAtForClassification("999999")).isEqualTo(LocalDate.now().toString())
    }

    @Test
    fun `codesAtForClassification rejects invalid date values`() {
        val configuration =
            configuration(
                classifications =
                    listOf(
                        classification("subject-fields", "618", date = "yesterday"),
                        classification("unit-types", "702", date = "2024-08-01"),
                        classification("measurement-type", "303", date = "today", levels = listOf(1)),
                    ),
            )

        assertThrows<IllegalStateException> {
            configuration.codesAtForClassification("702")
        }
    }
}
