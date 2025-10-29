package no.ssb.metadata.vardef.utils

import io.micronaut.json.JsonMapper
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.restassured.RestAssured
import io.restassured.RestAssured.oauth2
import io.restassured.filter.log.RequestLoggingFilter
import io.restassured.filter.log.ResponseLoggingFilter
import jakarta.inject.Inject
import no.ssb.metadata.vardef.integrations.vardok.models.VardokVardefIdPair
import no.ssb.metadata.vardef.integrations.vardok.repositories.VardokIdMappingRepository
import no.ssb.metadata.vardef.repositories.VariableDefinitionRepository
import no.ssb.metadata.vardef.services.PatchesService
import no.ssb.metadata.vardef.services.ValidityPeriodsService
import no.ssb.metadata.vardef.services.VariableDefinitionService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
open class BaseVardefTest {
    init {
        if (RestAssured.filters() == null) {
            RestAssured.filters(RequestLoggingFilter(), ResponseLoggingFilter())
        }
        RestAssured.authentication = oauth2(JwtTokenHelper.jwtTokenSigned().parsedString)
    }

    @Inject
    lateinit var variableDefinitionRepository: VariableDefinitionRepository

    @Inject
    lateinit var vardokIdMappingRepository: VardokIdMappingRepository

    @Inject
    lateinit var variableDefinitionService: VariableDefinitionService

    @Inject
    lateinit var validityPeriods: ValidityPeriodsService

    @Inject
    lateinit var patches: PatchesService

    @Inject
    lateinit var jsonMapper: JsonMapper

    @BeforeEach
    fun setUp() {
        variableDefinitionRepository.deleteAll()
        vardokIdMappingRepository.deleteAll()

        ALL_INCOME_TAX_PATCHES.forEach { variableDefinitionRepository.save(it) }
        variableDefinitionRepository.save(DRAFT_BUS_EXAMPLE)
        variableDefinitionRepository.save(DRAFT_EXAMPLE_WITH_VALID_UNTIL)
        variableDefinitionRepository.save(DRAFT_COMPLEX_SHORT_NAME)
        variableDefinitionRepository.save(SAVED_DRAFT_DEADWEIGHT_EXAMPLE)
        ALL_SAVED_INTERNAL_PATCHES.forEach { variableDefinitionRepository.save(it) }
        variableDefinitionRepository.save(SAVED_BYDEL_WITH_ILLEGAL_SHORTNAME)
        variableDefinitionRepository.save(SAVED_TO_PUBLISH)
        variableDefinitionRepository.save(PATCH_MANDATORY_FIELDS)
        variableDefinitionRepository.save(SAVED_TO_PUBLISH_ILLEGAL_CONTACT)

        vardokIdMappingRepository.save(VardokVardefIdPair("005", DRAFT_BUS_EXAMPLE.definitionId))
    }
}
