package no.ssb.metadata.vardef.integrations.klass.validators

import no.ssb.metadata.vardef.integrations.klass.models.Classification
import no.ssb.metadata.vardef.integrations.klass.models.ClassificationLinks
import no.ssb.metadata.vardef.integrations.klass.models.Link

object KlassCodeUtil {
    fun isValid(value: String?): Boolean {
        return TestCacheObject.classificationItems.map { item -> item.id }.contains(value?.toInt())
    }
}

/* TODO: Only for testing, remove when cache object is implemented
* */
object TestCacheObject {
    val classificationItems =
        listOf(
            Classification(
                name = "Standard for delområde- og grunnkretsinndeling",
                id = 1,
                classificationType = "Klassifikasjon",
                lastModified = "2024-03-08T13:55:51.000+0000",
                links = ClassificationLinks(self = Link(href = "https://data.ssb.no/api/klass/v1/classifications/1")),
            ),
            Classification(
                name = "Standard for kjønn",
                id = 2,
                classificationType = "Klassifikasjon",
                lastModified = "2018-12-07T14:02:33.000+0000",
                links = ClassificationLinks(self = Link(href = "https://data.ssb.no/api/klass/v1/classifications/2")),
            ),
            Classification(
                name = "Standard for trafikanttype",
                id = 13,
                classificationType = "Klassifikasjon",
                lastModified = "2016-10-07T12:06:17.000+0000",
                links = ClassificationLinks(self = Link(href = "https://data.ssb.no/api/klass/v1/classifications/13")),
            ),
            Classification(
                name = "Standard for veitrafikkulykke",
                id = 14,
                classificationType = "Klassifikasjon",
                lastModified = "2016-10-07T12:06:17.000+0000",
                links = ClassificationLinks(self = Link("href=https://data.ssb.no/api/klass/v1/classifications/14")),
            ),
        )
}
