package no.ssb.metadata.controllers

import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Header
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Status
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import io.micronaut.validation.Validated
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.inject.Inject
import jakarta.validation.Valid
import no.ssb.metadata.models.VariableDefinitionDAO
import no.ssb.metadata.models.VariableDefinitionDTO
import no.ssb.metadata.repositories.VariableDefinitionRepository
import no.ssb.metadata.services.VariableDefinitionService

@Validated
@Controller("/variables")
@ExecuteOn(TaskExecutors.BLOCKING)
class VariablesController {
    @Inject
    lateinit var vardefService: VariableDefinitionService

    @Inject
    lateinit var repository: VariableDefinitionRepository

    @Get()
    fun listAllByLanguage(
        @Header("Accept-Language", defaultValue = "nb") language: String,
    ): List<VariableDefinitionDTO> {
        return vardefService.findByLanguage(language)
    }

    @Post()
    @Status(HttpStatus.CREATED)
    @ApiResponse(responseCode = "201", description = "Successfully created.")
    @ApiResponse(responseCode = "400", description = "Bad request.")
    fun createVariableDefinition(
        @Body @Valid vardef: VariableDefinitionDAO,
    ): VariableDefinitionDAO {
        return repository.save(vardef)
    }
}
