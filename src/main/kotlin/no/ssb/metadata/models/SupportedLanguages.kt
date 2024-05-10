package no.ssb.metadata.models

import io.micronaut.serde.annotation.Serdeable
import io.micronaut.serde.config.naming.LowerCaseStrategy

@Serdeable(naming=LowerCaseStrategy::class)
enum class SupportedLanguages {
    NB, NN, EN
}
