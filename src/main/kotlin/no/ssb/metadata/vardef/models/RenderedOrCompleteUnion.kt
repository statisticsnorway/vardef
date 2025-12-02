package no.ssb.metadata.vardef.models

import com.fasterxml.jackson.annotation.JsonUnwrapped
import io.micronaut.serde.annotation.Serdeable

/**
 * Rendered or complete
 *
 * A sealed class to simulate a Union class. This allows for returning
 * alternative representations of a resource from controller methods
 * without resorting to using `Any` as the type annotation.
 *
 * To use this we need to wrap it around the plain classes e.g.
 * @sample no.ssb.metadata.vardef.controllers.internalapi.VariableDefinitionByIdController.getVariableDefinitionById
 */
sealed class RenderedOrCompleteUnion {
    @Serdeable
    data class Rendered(
        @JsonUnwrapped
        val value: RenderedView,
    ) : RenderedOrCompleteUnion()

    @Serdeable
    data class Complete(
        @JsonUnwrapped
        val value: CompleteView,
    ) : RenderedOrCompleteUnion()
}
