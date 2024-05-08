package no.ssb.metadata

import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.*
import io.micronaut.validation.Validated
import jakarta.inject.Inject
import jakarta.validation.Valid
import reactor.core.publisher.Mono
import no.ssb.metadata.model.VariableDefinition
import no.ssb.metadata.model.VariableDefinitionRepository
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
    open fun createVariableDefinition(@Body @Valid vardef: VariableDefinition): Mono<VariableDefinition> {
        return vardefService.save(vardef)
    }
}