package no.ssb.metadata.vardef.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.*
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.exceptions.HttpStatusException
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import io.micronaut.validation.Validated
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.inject.Inject
import io.micronaut.http.client.HttpClient
import io.micronaut.http.HttpRequest
import java.net.URI
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import no.ssb.metadata.vardef.integrations.vardok.*
import no.ssb.metadata.vardef.models.InputVariableDefinition
import no.ssb.metadata.vardef.models.LanguageStringType
import no.ssb.metadata.vardef.models.VariableStatus

@Validated
@Controller("/variable-definitions/vardok-migration/{id}")
@ExecuteOn(TaskExecutors.BLOCKING)
class VarDokMigrationController {

    @Inject
    lateinit var varDokApiService: VarDokApiService

//    @Inject
//    lateinit var varDefService: varDefService

    @Client("/")
    @Inject
    lateinit var httpClient: HttpClient

    /**
     * Create a variable definition from a VarDok variable.
     *
     */
    @Post
    @Status(HttpStatus.CREATED)
    @ApiResponse(responseCode = "201", description = "Successfully created.")
    @ApiResponse(responseCode = "400", description = "Bad request.")
    fun createVariableDefinitionFromVarDok(id: String): InputVariableDefinition? {
        try {
            var varDefInput = varDokApiService.createVarDefInputFromVarDokItems(
                    varDokApiService.fetchMultipleVarDokItemsByLanguage(id),
                )

            val dateString = "2024-07-04"
            val formatter = DateTimeFormatter.ISO_LOCAL_DATE
            val localDate = LocalDate.parse(dateString, formatter)


            val varDefInput_2 = VarDokStructure(
                name = LanguageStringType("navn", null, null),
                shortName = "sname",
                definition = LanguageStringType("def", null, null),
                validFrom = "2024-07-04",
                validUntil = null,
                unitTypes = listOf("01"),
                externalReferenceUri = URI("https://www.ssb.no/a/xml/metadata/conceptvariable/vardok/2").toURL(),
                classificationReference = null,
                containsSensitivePersonalInformation = false,
                contact = null,
                measurementType = null,
                relatedVariableDefinitionUris = emptyList(),
                subjectFields = emptyList(),
            )

            val mapper = ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            val jsonFromVardoc = mapper.writeValueAsString(varDefInput_2)

            println(jsonFromVardoc)

            val response = httpClient.toBlocking().retrieve(
                HttpRequest.POST("/variable-definitions", varDefInput),
                InputVariableDefinition::class.java
            )

            return response

        } catch (e: VardokException) {
            throw HttpStatusException(HttpStatus.BAD_REQUEST, e.message)
        }
    }
}




//return vardefApiService.postNewVariableDefinition(varDefInput)
