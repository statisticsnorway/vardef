package no.ssb.metadata.vardef.integrations.vardok

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import kotlinx.coroutines.runBlocking
import no.ssb.metadata.vardef.integrations.vardok.services.VardokService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.argumentSet
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

@MicronautTest
class VardokResponseTest {
    @Inject
    lateinit var vardokService: VardokService

    @Test
    fun `calculation in response`() {
        val response = vardokService.getVardokItem("566")
        assertThat(response?.variable?.calculation).isNotNull
    }

    @Test
    fun `calculation not in response`() {
        val response = vardokService.getVardokItem("2")
        assertThat(response?.variable?.calculation).isEmpty()
    }

    @Test
    fun `relations in response and not classificationRelation`() {
        val response = vardokService.getVardokItem("2")
        assertThat(response?.relations).isNotNull()
        assertThat(response?.relations?.classificationRelation).isNull()
    }

    @Test
    fun `relations classificationRelation in response`() {
        val response = vardokService.getVardokItem("1919")
        assertThat(response?.relations?.classificationRelation).isNotNull()
        assertThat(response?.relations?.classificationRelation?.href).isEqualTo("http://www.ssb.no/classification/klass/91")
    }

    @Test
    fun `notes in response`() {
        val response = vardokService.getVardokItem("134")
        assertThat(response?.common?.notes).isEqualTo("Dokumentet det refereres til er \"Om statistikken\" som ligger på Internett.")
    }

    @Test
    fun `notes not in response`() {
        val response = vardokService.getVardokItem("2")
        assertThat(response?.common?.notes).isEmpty()
    }

    @Test
    fun `invalid external document`() {
        val response = vardokService.getVardokItem("1245")
        assertThat(response?.variable?.externalDocument).isEqualTo(
            "Notater 2015/32 Klassifisering av jordbruksbedrifter etter driftsform og størrelse",
        )
    }

    @Test
    fun `empty external document`() {
        val response = vardokService.getVardokItem("130")
        assertThat(response?.variable?.externalDocument).isEqualTo(
            "",
        )
    }

    @Test
    fun `conceptvariables in response`() {
        val response = vardokService.getVardokItem("2")
        assertThat(response?.relations?.conceptVariableRelations).isNotEmpty
    }

    @Test
    fun `conceptvariable not in response`() {
        val response = vardokService.getVardokItem("948")
        assertThat(response?.relations?.conceptVariableRelations).isEmpty()
    }

    @Test
    fun `conceptvariable single in response`() {
        val response = vardokService.getVardokItem("1245")
        assertThat(response?.relations?.conceptVariableRelations).isNotEmpty
        assertThat(
            response
                ?.relations
                ?.conceptVariableRelations
                ?.get(0)
                ?.href,
        ).isEqualTo("http://www.ssb.no/conceptvariable/vardok/1246")
    }

    @ParameterizedTest
    @MethodSource("mapCommentField")
    fun `map Vardok notes and calculation to vardef comment`(
        vardokId: String,
        expectedCommentNB: String?,
        expectedCommentNN: String?,
        expectedCommentEN: String?,
        isConcatenated: Boolean,
    ) {
        val vardok = vardokService.getVardokItem(vardokId)
        val notes = vardok?.common?.notes
        val calculation = vardok?.variable?.calculation
        val varDefInput = runBlocking { vardokService.fetchMultipleVardokItemsByLanguage(vardokId) }
        val vardokTransform = VardokService.extractVardefInput(varDefInput)
        assertThat(vardokTransform.comment?.nb).isEqualTo(expectedCommentNB)
        assertThat(vardokTransform.comment?.nn).isEqualTo(expectedCommentNN)
        assertThat(vardokTransform.comment?.en).isEqualTo(expectedCommentEN)
        if (isConcatenated) {
            assertThat(vardokTransform.comment?.nb).containsSubsequence(notes, calculation)
        }
    }

    @Test
    fun `nb language is primary language`() {
        val response = vardokService.getVardokItem("948")
        assertThat(response?.xmlLang).isEqualTo("nb")
        assertThat(response?.xmlLang).isNotEqualTo("nn")
    }

    @Test
    fun `nn language is primary language`() {
        val response = vardokService.getVardokItem("2413")
        assertThat(response?.xmlLang).isEqualTo("nn")
        assertThat(response?.xmlLang).isNotEqualTo("nb")
    }

    companion object {
        @JvmStatic
        fun mapCommentField(): Stream<Arguments> =
            Stream.of(
                argumentSet(
                    "comment is null when notes and calculation are null",
                    "2",
                    null,
                    null,
                    null,
                    false,
                ),
                argumentSet(
                    "comment is notes when notes is not null and calculation is null",
                    "901",
                    "Opplysningene er hentet fra boligskjemaet i FoB2001, spørsmål 21.",
                    null,
                    "The information is collected from the Housing form in Census 2001, question 21.",
                    false,
                ),
                argumentSet(
                    "comment is calculation when calculation is not null and notes is null",
                    "267",
                    "= P8005 + P8006",
                    null,
                    null,
                    false,
                ),
                argumentSet(
                    "comment has nn language",
                    "1849",
                    "Byggekostnadsindeks for boliger er en veid indeks av byggekostnadsindeks for enebolig av tre og " +
                        "byggekostnadsindeks for boligblokk.",
                    "Byggjekostnadsindeks for bustader i alt er ein vege indeks av einebustader av tre " +
                        "og bustadblokker.",
                    null,
                    false,
                ),
                argumentSet(
                    "comment concatenates calculation and notes when both have values",
                    "1299",
                    "Denne variabelen benyttes både for foretak og bedrift.Beregnes via Næringsoppgaven: " +
                        "Post 9000/9900-post3400-post 3800/3895",
                    null,
                    null,
                    true,
                ),
            )
    }
}
