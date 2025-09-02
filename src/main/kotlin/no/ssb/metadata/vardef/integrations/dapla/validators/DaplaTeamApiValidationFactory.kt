package no.ssb.metadata.vardef.integrations.dapla.validators

import io.micronaut.context.annotation.Factory
import io.micronaut.validation.validator.constraints.ConstraintValidator
import jakarta.inject.Inject
import jakarta.inject.Singleton
import no.ssb.metadata.vardef.annotations.DaplaGroup
import no.ssb.metadata.vardef.annotations.DaplaTeam
import no.ssb.metadata.vardef.integrations.dapla.services.DaplaTeamService

@Factory
class DaplaTeamApiValidationFactory {
    @Inject
    private lateinit var daplaTeamApiService: DaplaTeamService

    @Singleton
    fun daplaTeamValidator(): ConstraintValidator<DaplaTeam, String> =
        ConstraintValidator {
            value,
            _,
            _,
            ->
            value == null || daplaTeamApiService.isValidTeam(value)
        }

    @Singleton
    fun daplaGroupValidator(): ConstraintValidator<DaplaGroup, String> =
        ConstraintValidator {
            value,
            _,
            _,
            ->
            value == null || daplaTeamApiService.isValidGroup(value)
        }
}
