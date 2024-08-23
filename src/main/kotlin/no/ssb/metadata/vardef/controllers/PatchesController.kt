package no.ssb.metadata.vardef.controllers

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Header
import io.micronaut.http.annotation.PathVariable
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import io.micronaut.validation.Validated
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.inject.Inject
import no.ssb.metadata.vardef.constants.ID_FIELD_DESCRIPTION
import no.ssb.metadata.vardef.models.RenderedVariableDefinition
import no.ssb.metadata.vardef.models.SupportedLanguages
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
        @Header("Accept-Language", defaultValue = "nb") language: SupportedLanguages,
    ): List<RenderedVariableDefinition> = varDefService.listAllPatchesByIdAndRenderForLanguage(variableDefinitionId, language)

    @Get("/{patch-id}")
    fun getOnePatch(
        @PathVariable("variable-definition-id") @Schema(description = ID_FIELD_DESCRIPTION) @VardefId variableDefinitionId: String,
        @PathVariable("patch-id") patchId: Int,
        @Header("Accept-Language", defaultValue = "nb") language: SupportedLanguages,
    ): RenderedVariableDefinition = varDefService.getOnePatchByIdAndRenderForLanguage(variableDefinitionId, patchId, language)
}
