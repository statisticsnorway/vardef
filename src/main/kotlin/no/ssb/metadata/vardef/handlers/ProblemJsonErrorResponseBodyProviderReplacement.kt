package no.ssb.metadata.vardef.handlers

import io.micronaut.context.annotation.Replaces
import io.micronaut.http.server.exceptions.response.ErrorContext
import io.micronaut.problem.ProblemJsonErrorResponseBodyProvider
import io.micronaut.problem.conf.ProblemConfiguration
import jakarta.inject.Singleton

/**
 * Don't suppress details for any exceptions
 */
@Replaces(ProblemJsonErrorResponseBodyProvider::class)
@Singleton
class ProblemJsonErrorResponseBodyProviderReplacement
    internal constructor(
        config: ProblemConfiguration?,
    ) : ProblemJsonErrorResponseBodyProvider(config) {
        /**
         * Include error message for all exceptions
         */
        override fun includeErrorMessage(errorContext: ErrorContext): Boolean = true
    }
