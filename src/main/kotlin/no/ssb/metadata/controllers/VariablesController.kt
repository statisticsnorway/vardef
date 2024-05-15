package no.ssb.metadata.controllers

import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Status
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import io.micronaut.validation.Validated
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.inject.Inject
import jakarta.validation.Valid
import no.ssb.metadata.models.VariableDefinition
import no.ssb.metadata.repositories.VariableDefinitionRepository

@Validated
@Controller("/variables")
@ExecuteOn(TaskExecutors.BLOCKING)
class VariablesController {
    @Inject
    lateinit var vardefService: VariableDefinitionRepository

    @Get()
    fun list(): List<VariableDefinition> {
        return vardefService.findAll().toList()
    }

    @Post()
    @Status(HttpStatus.CREATED)
    @ApiResponse(responseCode = "201", description = "Successfuly created.")
    @ApiResponse(responseCode = "400", description = "Bad request.")
    fun createVariableDefinition(
        @Body @Valid vardef: VariableDefinition,
    ): VariableDefinition {
        return vardefService.save(vardef)
    }
}
