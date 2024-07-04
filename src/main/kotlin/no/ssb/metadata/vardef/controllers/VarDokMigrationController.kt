package no.ssb.metadata.vardef.controllers

import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Status
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import io.micronaut.validation.Validated
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.inject.Inject
import no.ssb.metadata.vardef.integrations.vardok.VarDokApiService
import no.ssb.metadata.vardef.models.InputVariableDefinition
import no.ssb.metadata.vardef.services.VariableDefinitionService

@Validated
@Controller("/variable-definitions/vardok-migration/{id}")
@ExecuteOn(TaskExecutors.BLOCKING)
class VarDokMigrationController {
    @Inject
    lateinit var varDefService: VariableDefinitionService

    @Inject
    lateinit var varDokApiService: VarDokApiService

    /**
     * Create a variable definition from a VarDok variable.
     *
     * New variable definitions must have status DRAFT and include all required fields.
     */
    @Post()
    @Status(HttpStatus.CREATED)
    @ApiResponse(responseCode = "201", description = "Successfully created.")
    @ApiResponse(responseCode = "400", description = "Bad request.")
    fun createVariableDefinitionFromVarDok(id: String): InputVariableDefinition {
        val varDefInput = varDokApiService.createVarDefInputFromVarDokItems(varDokApiService.fetchMultipleVarDokItemsByLanguage(id))
        return varDefService.save(varDefInput.toSavedVariableDefinition()).toInputVariableDefinition()
    }
}
