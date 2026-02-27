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
import io.swagger.v3.oas.annotations.media.ArraySchema
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
import no.ssb.metadata.vardef.integrations.vardok.models.VardokVardefIdPairResponse
import no.ssb.metadata.vardef.integrations.vardok.services.VardokService
import no.ssb.metadata.vardef.models.CompleteView
import no.ssb.metadata.vardef.security.VARIABLE_CONSUMER
import no.ssb.metadata.vardef.security.VARIABLE_CREATOR
import no.ssb.metadata.vardef.services.VariableDefinitionService
import org.slf4j.LoggerFactory

@Tag(name = DATA_MIGRATION)
@Validated
@Controller("/vardok-migration")
@Secured(VARIABLE_CONSUMER)
@SecurityRequirement(name = LABID_TOKEN_SCHEME)
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
                    schema = Schema(implementation = CompleteView::class),
                    mediaType = MediaType.APPLICATION_JSON,
                    examples = [
                        ExampleObject(
                            name = "Migrate Vardok",
                            value = COMPLETE_VIEW_EXAMPLE,
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
                    .POST("/variable-definitions", vardefInput.toString())
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
     * Get a vardok id by vardef id.
     */
    @Get("{vardef-id:[-a-zA-Z0-9_][-a-zA-Z0-9_][-a-zA-Z0-9_][-a-zA-Z0-9_][-a-zA-Z0-9_][-a-zA-Z0-9_][-a-zA-Z0-9_][-a-zA-Z0-9_]}") // This pattern can't use quantifiers due to a limitation in Micronaut
    @NotFoundApiResponse
    @ApiResponse(
        content =
            [
                Content(
                    schema = Schema(VardokIdResponse::class),
                    mediaType = MediaType.APPLICATION_JSON,
                    examples = [
                        ExampleObject(
                            name = "Vardef id",
                            value = VARDOK_ID_RESPONSE_EXAMPLE,
                        ),
                    ],
                ),
            ],
    )
    fun getVardokByVardefId(
        @Parameter(
            name = "vardef-id",
            description = "The ID of a variable definition which has been migrated.",
            schema = Schema(pattern = "[-a-zA-Z0-9_]{8}"),
            examples = [
                ExampleObject(
                    name = "Vardef id",
                    value = VARDEF_ID_MIGRATED_EXAMPLE,
                ),
                ExampleObject(
                    name = NOT_FOUND_EXAMPLE_NAME,
                    value = VARDEF_ID_NOT_MIGRATED_EXAMPLE,
                ),
            ],
        )
        @PathVariable("vardef-id")
        vardefId: String,
    ): VardokIdResponse =
        vardokService
            .getVardokIdByVardefId(vardefId)
            ?.let { VardokIdResponse(it) }
            ?: throw HttpStatusException(
                HttpStatus.NOT_FOUND,
                "No vardok mapping for vardef id $vardefId",
            )

    /**
     * Get a variable definition by vardok id.
     */
    @Get("{vardok-id:\\d|\\d\\d|\\d\\d\\d|\\d\\d\\d\\d|\\d\\d\\d\\d\\d}") // This pattern can't use quantifiers due to a limitation in Micronaut
    @NotFoundApiResponse
    @ApiResponse(
        content =
            [
                Content(
                    schema = Schema(CompleteView::class),
                    mediaType = MediaType.APPLICATION_JSON,
                    examples = [
                        ExampleObject(
                            name = "Vardok id",
                            value = COMPLETE_VIEW_EXAMPLE,
                        ),
                    ],
                ),
            ],
    )
    fun getVardefByVardokId(
        @Parameter(
            name = "vardok-id",
            description = "The ID of the definition in Vardok.",
            schema = Schema(pattern = "\\d{1,5}"),
            examples = [
                ExampleObject(
                    name = "Vardok id",
                    value = "1607",
                ),
                ExampleObject(
                    name = NOT_FOUND_EXAMPLE_NAME,
                    value = "9999",
                ),
            ],
        )
        @PathVariable("vardok-id")
        vardokId: String,
        httpRequest: HttpRequest<*>,
    ): MutableHttpResponse<*> {
        val vardefId = vardokService.getVardefIdByVardokId(vardokId)
        val request =
            HttpRequest
                .GET<String>("/variable-definitions/$vardefId")
                .headers {
                    it[AUTHORIZATION] = httpRequest.headers[AUTHORIZATION]
                }

        return runBlocking {
            httpClient.proxy(request).awaitFirst()
        }
    }

    /**
     * Get a list of all vardok and vardef id mappings
     */
    @Produces(MediaType.APPLICATION_JSON)
    @Get()
    @ApiResponse(
        content = [
            Content(
                examples = [
                    ExampleObject(
                        name = "Vardef vardok id list",
                        value = VARDOK_VARDEF_ID_LIST_RESPONSE_EXAMPLE,
                    ),
                ],
                array = ArraySchema(schema = Schema(implementation = VardokVardefIdPairResponse::class)),
            ),
        ],
    )
    fun listVardokVardefMappings(): List<VardokVardefIdPairResponse> = vardokService.listVardokVardefMappings()
}
