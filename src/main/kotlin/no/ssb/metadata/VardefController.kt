package no.ssb.metadata

import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.*
import io.micronaut.validation.Validated
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.inject.Inject
import jakarta.validation.Valid
import reactor.core.publisher.Mono
import no.ssb.metadata.models.VariableDefinition
import no.ssb.metadata.models.VariableDefinitionRepository
import org.reactivestreams.Publisher

@Validated
@Controller("/variables")
class VardefController {

    @Inject
    lateinit var vardefService: VariableDefinitionRepository

    @Get()
    fun list(): Publisher<VariableDefinition> {
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