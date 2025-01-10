package no.ssb.metadata.vardef.integrations.vardok.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import no.ssb.metadata.vardef.integrations.vardok.models.VardokResponse
import org.junit.jupiter.api.BeforeEach

@MicronautTest
open class BaseVardokTest {
    @Inject
    lateinit var xmlMapper: ObjectMapper

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
        xmlMapper = XmlMapper().registerKotlinModule()
        vardokResponse1 = xmlMapper.readValue(vardokId1466validFromDateAndOtherLanguages)
        vardokResponse2 = xmlMapper.readValue(vardokId49validUntilDate)
        vardokResponse3 = xmlMapper.readValue(vardokId476validFromDateAndNNInOtherLanguages)
        vardokResponse4 = xmlMapper.readValue(vardokId120validUntilDateAndOtherLanguages)
        vardokResponse5 = xmlMapper.readValue(vardokId100NoValidDates)
        vardokResponse6 = xmlMapper.readValue(vardokId123NoDataElementName)
        vardokResponse7 = xmlMapper.readValue(vardokId2677UnitTypeList)
        vardokResponse8 = xmlMapper.readValue(vardokId123NoDataElementNameEn)
    }
}
