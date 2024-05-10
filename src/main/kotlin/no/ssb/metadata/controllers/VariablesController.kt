package no.ssb.metadata.controllers

import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.*
import io.micronaut.validation.Validated
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.inject.Inject
import jakarta.validation.Valid
import no.ssb.metadata.models.VariableDefinition
import no.ssb.metadata.repositories.VariableDefinitionRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Validated
@Controller("/variables")
class VariablesController {

    @Inject
    lateinit var vardefService: VariableDefinitionRepository

    @Get()
    fun list(): Flux<VariableDefinition> {
        return vardefService.list()
    }

    @Post()
    @Status(HttpStatus.CREATED)
    @ApiResponse(responseCode = "201", description = "Successfully created.")
    @ApiResponse(responseCode = "400", description = "Bad request. See the message for more details.")
    open fun createVariableDefinition(@Body @Valid vardef: VariableDefinition): Mono<VariableDefinition> {
        return vardefService.save(vardef)
    }
}