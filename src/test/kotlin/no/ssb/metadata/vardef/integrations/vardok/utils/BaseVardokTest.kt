package no.ssb.metadata.vardef.integrations.vardok.utils

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import no.ssb.metadata.vardef.integrations.vardok.models.VardokResponse
import org.junit.jupiter.api.BeforeEach
import tools.jackson.dataformat.xml.XmlMapper

@MicronautTest
open class BaseVardokTest {
    @Inject
    lateinit var xmlMapper: XmlMapper

    lateinit var vardokResponse1: VardokResponse
    lateinit var vardokResponse2: VardokResponse
    lateinit var vardokResponse3: VardokResponse
    lateinit var vardokResponse4: VardokResponse
    lateinit var vardokResponse5: VardokResponse
    lateinit var vardokResponse6: VardokResponse
    lateinit var vardokResponse7: VardokResponse
    lateinit var vardokResponse8: VardokResponse

    @BeforeEach
    open fun setUp() {
        xmlMapper = XmlMapper()
        vardokResponse1 = xmlMapper.readValue(vardokId1466validFromDateAndOtherLanguages, VardokResponse::class.java)
        vardokResponse2 = xmlMapper.readValue(vardokId49validUntilDate, VardokResponse::class.java)
        vardokResponse3 = xmlMapper.readValue(vardokId476validFromDateAndNNInOtherLanguages, VardokResponse::class.java)
        vardokResponse4 = xmlMapper.readValue(vardokId120validUntilDateAndOtherLanguages, VardokResponse::class.java)
        vardokResponse5 = xmlMapper.readValue(vardokId100NoValidDates, VardokResponse::class.java)
        vardokResponse6 = xmlMapper.readValue(vardokId123NoDataElementName, VardokResponse::class.java)
        vardokResponse7 = xmlMapper.readValue(vardokId2677UnitTypeList, VardokResponse::class.java)
        vardokResponse8 = xmlMapper.readValue(vardokId123NoDataElementNameEn, VardokResponse::class.java)
    }
}
