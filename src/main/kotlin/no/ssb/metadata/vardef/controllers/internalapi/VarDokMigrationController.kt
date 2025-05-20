package no.ssb.metadata.vardef.controllers.internalapi

import io.micronaut.http.*
import io.micronaut.http.HttpHeaders.AUTHORIZATION
import io.micronaut.http.annotation.*
import io.micronaut.http.client.ProxyHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.exceptions.HttpStatusException
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import io.micronaut.security.annotation.Secured
import io.micronaut.validation.Validated
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.runBlocking
import no.ssb.metadata.vardef.annotations.BadRequestApiResponse
import no.ssb.metadata.vardef.annotations.NotFoundApiResponse
import no.ssb.metadata.vardef.constants.*
import no.ssb.metadata.vardef.integrations.vardok.models.VardefInput
import no.ssb.metadata.vardef.integrations.vardok.models.VardokIdResponse
import no.ssb.metadata.vardef.integrations.vardok.models.VardokNotFoundException
import no.ssb.metadata.vardef.integrations.vardok.services.VardokService
import no.ssb.metadata.vardef.models.CompleteResponse
import no.ssb.metadata.vardef.security.VARIABLE_CONSUMER
import no.ssb.metadata.vardef.security.VARIABLE_CREATOR
import no.ssb.metadata.vardef.services.VariableDefinitionService
import org.slf4j.LoggerFactory

@Tag(name = DATA_MIGRATION)
@Validated
@Controller("/vardok-migration")
@Secured(VARIABLE_CONSUMER)
@SecurityRequirement(name = KEYCLOAK_TOKEN_SCHEME)
@ExecuteOn(TaskExecutors.BLOCKING)
class VarDokMigrationController(
    private val vardokService: VardokService,
    private val vardefService: VariableDefinitionService,
    @Client("/") private val httpClient: ProxyHttpClient,
) {
    private val logger = LoggerFactory.getLogger(VarDokMigrationController::class.java)

    /**
     * Create a variable definition from a VarDok variable definition.
     */
    @Post("/{vardok-id}")
    @Status(HttpStatus.CREATED)
    @ApiResponse(
        responseCode = "201",
        description = "Successfully created.",
        content =
            [
                Content(
                    schema = Schema(implementation = CompleteResponse::class),
                    mediaType = MediaType.APPLICATION_JSON,
                    examples = [
                        ExampleObject(
                            name = "Migrate Vardok",
                            value = COMPLETE_RESPONSE_EXAMPLE,
                        ),
                    ],
                ),
            ],
    )
    @BadRequestApiResponse
    @Secured(VARIABLE_CREATOR)
    fun createVariableDefinitionFromVarDok(
        @Parameter(
            name = "vardok-id",
            description = "The ID of the definition in Vardok.",
            examples = [
                ExampleObject(
                    name = "Migrate Vardok",
                    value = "1607",
                ),
            ],
        )
        @PathVariable("vardok-id")
        id: String,
        @Parameter(
            name = ACTIVE_GROUP,
            description = ACTIVE_GROUP_QUERY_PARAMETER_DESCRIPTION,
            examples = [
                ExampleObject(
                    name = "Migrate Vardok",
                    value = ACTIVE_GROUP_EXAMPLE,
                ),
            ],
        )
        @QueryValue(ACTIVE_GROUP)
        activeGroup: String,
        httpRequest: HttpRequest<*>,
    ): MutableHttpResponse<*> {
        if (vardokService.isAlreadyMigrated(id)) {
            throw HttpStatusException(
                HttpStatus.CONFLICT,
                "Vardok definition with ID $id already migrated and may not be migrated again.",
            )
        }

        val vardefInput: VardefInput
        try {
            vardefInput =
                VardokService.extractVardefInput(
                    vardokService.fetchMultipleVardokItemsByLanguage(id),
                )
        } catch (e: VardokNotFoundException) {
            // We always want to return NOT_FOUND in this case
            throw HttpStatusException(HttpStatus.NOT_FOUND, e.message)
        } catch (e: Exception) {
            // Other validation exceptions
            throw HttpStatusException(HttpStatus.BAD_REQUEST, e.message)
        }

        val createVariableDefinitionResponse =
            httpClient.proxy(
                HttpRequest
                    .POST("/variable-definitions?$ACTIVE_GROUP=$activeGroup", vardefInput.toString())
                    .headers { entries: MutableHttpHeaders ->
                        entries.set(AUTHORIZATION, httpRequest.headers.get(AUTHORIZATION))
                    },
            )

        val response: MutableHttpResponse<*>
        runBlocking {
            response = createVariableDefinitionResponse.awaitFirst()
            if (response.code() == HttpStatus.CREATED.code) {
                val vardefId = vardefInput.shortName?.let { vardefService.getByShortName(it)?.id }
                if (vardefId != null) {
                    vardokService
                        .createVardokVardefIdMapping(vardokId = id, vardefId = vardefId)
                        .also { mapping ->
                            logger.info("Created Vardok ID mapping $mapping")
                        }
                } else {
                    logger.warn("Could not find corresponding Vardef ID to map to Vardok ID $id")
                }
            }
        }

        return response
    }

    /**
     * Get one variable definition by vardok id or get the vardok id by vardef id.
     */
    @Get("/{id}")
    @ApiResponse(
        content =
            [
                Content(
                    schema = Schema(implementation = CompleteResponse::class),
                    mediaType = MediaType.APPLICATION_JSON,
                    examples = [
                        ExampleObject(
                            name = "Vardok id",
                            value = COMPLETE_RESPONSE_EXAMPLE,
                        ),
                        ExampleObject(
                            name = "Vardef id",
                            value = VARDOK_ID_RESPONSE_EXAMPLE,
                        ),
                    ],
                ),
            ],
    )
    @NotFoundApiResponse
    fun getCorrespodingVariableDefinitionById(
        @Parameter(
            name = "vardok-or-vardef-id",
            description = "The ID of the definition in Vardok or Vardef.",
            examples = [
                ExampleObject(
                    name = "Vardok id",
                    value = "1607",
                ),
                ExampleObject(
                    name = "Vardef id",
                    value = "DKJcm_E2",
                ),
            ],
        )
        @PathVariable("id")
        id: String,
        @Parameter(
            name = ACTIVE_GROUP,
            description = ACTIVE_GROUP_QUERY_PARAMETER_DESCRIPTION,
            examples = [
                ExampleObject(
                    name = "Migrate Vardok",
                    value = ACTIVE_GROUP_EXAMPLE,
                ),
            ],
        )
        @QueryValue(ACTIVE_GROUP)
        activeGroup: String,
        httpRequest: HttpRequest<*>,
    ): MutableHttpResponse<*> {
        val vardefIdRegex = VARDEF_ID_PATTERN.toRegex()
        if (!vardefIdRegex.containsMatchIn(id)) {
            val vardefId = vardokService.getVardefIdByVardokId(id)

            val getVariableDefinitionResponse =
                httpClient.proxy(
                    HttpRequest
                        .GET<String>("/variable-definitions/$vardefId?$ACTIVE_GROUP=$activeGroup")
                        .headers { entries: MutableHttpHeaders ->
                            entries.set(AUTHORIZATION, httpRequest.headers.get(AUTHORIZATION))
                        },
                )

            val response: MutableHttpResponse<*>
            runBlocking {
                response = getVariableDefinitionResponse.awaitFirst()
            }
            return response
        } else {
            return vardokService
                .getVardokIdByVardefId(id)
                ?.let { HttpResponse.ok(VardokIdResponse(it)) }
                ?: HttpResponse.notFound(HttpStatus.NOT_FOUND)
        }
    }

    /**
     * Get a list of all vardok and vardef id mappings
     */
    @Produces(MediaType.APPLICATION_JSON)
    @Get()
    fun getCorrespondingVariableDefinitions(httpRequest: HttpRequest<*>): MutableHttpResponse<*> =
        HttpResponse.ok(vardokService.getVardokVardefIdMapping())
}
