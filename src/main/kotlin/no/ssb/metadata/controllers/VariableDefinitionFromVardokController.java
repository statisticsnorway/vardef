package no.ssb.metadata.controllers;


import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Produces;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.validation.Validated;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.inject.Inject;
import no.ssb.metadata.services.VariableDefinitionService;

@Validated
@Controller("/variable-definitions/migrate-from-var-doc/{id}")
@ExecuteOn(TaskExecutors.BLOCKING)
public class VariableDefinitionFromVardokController {

    /**
     * Get one variable definition.
     *
     * This is rendered in the given language, with the default being Norwegian Bokmål.
     */
//    @Produces(MediaType.APPLICATION_JSON)
//    @ApiResponse(responseCode = "404", description = "No such variable definition found")
//    @Get()
    //fun getVariableFromVardokById(id: String):


}




