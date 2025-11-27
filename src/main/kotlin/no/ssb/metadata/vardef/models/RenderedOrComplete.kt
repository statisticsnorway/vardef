package no.ssb.metadata.vardef.models

import com.fasterxml.jackson.annotation.JsonUnwrapped
import io.micronaut.serde.annotation.Serdeable

sealed class RenderedOrComplete {
    @Serdeable
    data class SealedRenderedVariableDefinition(
        @JsonUnwrapped
        val value: RenderedVariableDefinition,
    ) : RenderedOrComplete()

    @Serdeable
    data class SealedCompleteResponse(
        @JsonUnwrapped
        val value: CompleteResponse,
    ) : RenderedOrComplete()
}
