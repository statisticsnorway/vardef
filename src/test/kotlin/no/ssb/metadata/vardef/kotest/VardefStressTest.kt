package no.ssb.metadata.vardef.kotest

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import io.micronaut.security.authentication.Authentication
import io.micronaut.test.extensions.kotest5.MicronautKotest5Extension
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import kotlinx.coroutines.*
import no.ssb.metadata.vardef.controllers.VariableDefinitionsController
import no.ssb.metadata.vardef.models.CompleteResponse
import no.ssb.metadata.vardef.models.Contact
import no.ssb.metadata.vardef.models.Draft
import no.ssb.metadata.vardef.models.LanguageStringType
import no.ssb.metadata.vardef.utils.TEST_DEVELOPERS_GROUP
import no.ssb.metadata.vardef.utils.TEST_USER
import org.slf4j.LoggerFactory
import java.net.URI
import java.time.LocalDate
import kotlin.random.Random

@Suppress("unused")
object KotestConfig : AbstractProjectConfig() {
    override val autoScanEnabled = false

    override fun extensions() = listOf(MicronautKotest5Extension)

    override val parallelism = 2
    override val isolationMode = IsolationMode.InstancePerLeaf
}

@MicronautTest
@Requires(env = ["integration-test"])
class VardefStressTest(
    private val variableDefinitionsController: VariableDefinitionsController,
) : ShouldSpec({

        val logger = LoggerFactory.getLogger(VardefStressTest::class.java)
        val numOfDefinitions = 1000
        val repeatCount = 4

        fun repeatTest(
            times: Int,
            block: suspend () -> Unit,
        ) = runBlocking {
            repeat(times) { block() }
        }

        should("create variable definitions") {
            repeatTest(repeatCount) {
                runBlocking {
                    val result =
                        (1..numOfDefinitions).map { i ->
                            async {
                                try {
                                    val draft =
                                        Draft(
                                            name =
                                                LanguageStringType(
                                                    nb = "Buss",
                                                    nn = "Buss",
                                                    en = "Bus",
                                                ),
                                            shortName = "bus$i",
                                            definition =
                                                LanguageStringType(
                                                    nb =
                                                        "En buss er en bil for persontransport med over 8 sitteplasser " +
                                                            "i tillegg til førersetet.",
                                                    nn = null,
                                                    en = "A bus is",
                                                ),
                                            classificationReference = "91",
                                            unitTypes = listOf("02"),
                                            subjectFields = listOf("al"),
                                            containsSpecialCategoriesOfPersonalData = false,
                                            measurementType = null,
                                            validFrom = LocalDate.of(2025, 1, 1),
                                            validUntil = null,
                                            externalReferenceUri = URI("https://www.example.com").toURL(),
                                            comment = null,
                                            relatedVariableDefinitionUris =
                                                listOf(
                                                    URI("https://www.example.com").toURL(),
                                                ),
                                            contact =
                                                Contact(
                                                    LanguageStringType("Sjefen", "", ""),
                                                    "sjef@ssb.no",
                                                ),
                                        )

                                    variableDefinitionsController.createVariableDefinition(
                                        draft = draft,
                                        activeGroup = TEST_DEVELOPERS_GROUP,
                                        authentication = Authentication.build(TEST_USER),
                                    )
                                } catch (e: HttpStatusException) {
                                    e.status
                                }
                            }
                        }.awaitAll()
                    val successCount = result.count { it is CompleteResponse }
                    val conflictCount = result.count { it == HttpStatus.CONFLICT }
                    successCount shouldBe (numOfDefinitions - conflictCount)
                }
            }
        }

        should("return the correct number of variable definitions") {
            val result = variableDefinitionsController.listVariableDefinitions()
            result.size shouldBe numOfDefinitions
        }

        should("create variable definitions concurrently") {
            repeatTest(repeatCount) {
                runBlocking {
                    (1..repeatCount).map { runId ->
                        async(Dispatchers.IO) {
                            logger.info("Starting parallel run #$runId")
                            val result =
                                (1..numOfDefinitions).map { _ ->
                                    async {
                                        try {
                                            val randomNum = Random.nextInt(100, 888)
                                            val draft =
                                                Draft(
                                                    name =
                                                        LanguageStringType(
                                                            nb = "Buss",
                                                            nn = "Buss",
                                                            en = "Bus",
                                                        ),
                                                    shortName = "bike_$randomNum",
                                                    definition =
                                                        LanguageStringType(
                                                            nb =
                                                                "En buss er en bil for persontransport med over 8 " +
                                                                    "sitteplasser i tillegg til førersetet.",
                                                            nn = null,
                                                            en = "A bus is",
                                                        ),
                                                    classificationReference = "91",
                                                    unitTypes = listOf("02"),
                                                    subjectFields = listOf("al"),
                                                    containsSpecialCategoriesOfPersonalData = false,
                                                    measurementType = null,
                                                    validFrom = LocalDate.of(2025, 1, 1),
                                                    validUntil = null,
                                                    externalReferenceUri = URI("https://www.example.com").toURL(),
                                                    comment = null,
                                                    relatedVariableDefinitionUris =
                                                        listOf(
                                                            URI("https://www.example.com").toURL(),
                                                        ),
                                                    contact =
                                                        Contact(
                                                            LanguageStringType("Sjefen", "", ""),
                                                            "sjef@ssb.no",
                                                        ),
                                                )

                                            variableDefinitionsController.createVariableDefinition(
                                                draft = draft,
                                                activeGroup = TEST_DEVELOPERS_GROUP,
                                                authentication = Authentication.build(TEST_USER),
                                            )
                                        } catch (e: HttpStatusException) {
                                            e.status
                                        }
                                    }
                                }.awaitAll()
                            val successCount = result.count { it is CompleteResponse }
                            val conflictCount = result.count { it == HttpStatus.CONFLICT }
                            successCount shouldBe (numOfDefinitions - conflictCount)
                        }
                    }.awaitAll()
                }
            }
        }
    })
