package no.ssb.metadata.vardef.integrations.dapla.validators

import io.micronaut.context.annotation.Factory
import io.micronaut.validation.validator.constraints.ConstraintValidator
import jakarta.inject.Inject
import jakarta.inject.Singleton
import no.ssb.metadata.vardef.integrations.dapla.service.DaplaTeamApiService

@Factory
class DaplaTeamApiValidationFactory {
    @Inject
    private lateinit var daplaTeamApiService: DaplaTeamApiService

    @Singleton
    fun daplaTeamValidator(): ConstraintValidator<DaplaTeam, String> =
        ConstraintValidator {
                value,
                _,
                _,
            ->
            value == null || daplaTeamApiService.isValidTeam(value)
        }
}
