package no.ssb.metadata.vardef.controllers

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import io.micronaut.validation.Validated
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.inject.Inject
import no.ssb.metadata.vardef.constants.ID_FIELD_DESCRIPTION
import no.ssb.metadata.vardef.models.InputVariableDefinition
import no.ssb.metadata.vardef.services.VariableDefinitionService
import no.ssb.metadata.vardef.validators.VardefId

@Validated
@Controller("/variable-definitions/{variable-definition-id}/patches")
@ExecuteOn(TaskExecutors.BLOCKING)
class PatchesController {
    @Inject
    lateinit var varDefService: VariableDefinitionService

    @Get
    fun getAllPatches(
        @PathVariable("variable-definition-id") @Schema(description = ID_FIELD_DESCRIPTION) @VardefId variableDefinitionId: String,
    ): MutableHttpResponse<List<InputVariableDefinition>> =
        HttpResponse
            .ok(varDefService.listAllPatchesById(id = variableDefinitionId).map { it.toInputVariableDefinition() })
            .contentType(MediaType.APPLICATION_JSON)

    @Get("/{patch-id}")
    fun getOnePatch(
        @PathVariable("variable-definition-id") @Schema(description = ID_FIELD_DESCRIPTION) @VardefId variableDefinitionId: String,
        @PathVariable("patch-id") patchId: Int,
    ): MutableHttpResponse<InputVariableDefinition> =
        HttpResponse
            .ok(varDefService.getOnePatchById(variableDefinitionId, patchId = patchId))
            .contentType(MediaType.APPLICATION_JSON)
}
