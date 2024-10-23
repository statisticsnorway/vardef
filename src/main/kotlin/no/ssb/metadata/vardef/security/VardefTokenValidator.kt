package no.ssb.metadata.vardef.security

import com.nimbusds.jwt.JWT
import io.micronaut.context.annotation.Property
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.token.Claims
import io.micronaut.security.token.jwt.validator.JsonWebTokenParser
import io.micronaut.security.token.jwt.validator.JwtAuthenticationFactory
import io.micronaut.security.token.jwt.validator.ReactiveJsonWebTokenValidator
import jakarta.inject.Inject
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono

class VardefTokenValidator<R> : ReactiveJsonWebTokenValidator<JWT, R> {
    private val logger = LoggerFactory.getLogger(VardefTokenValidator::class.java)

    @Property(name = "dapla.lab.security.audience")
    private lateinit var daplaLabAudience: String

    @Inject
    lateinit var jwtAuthenticationFactory: JwtAuthenticationFactory

    @Inject
    private lateinit var jsonWebTokenParser: JsonWebTokenParser<JWT>

    override fun validateToken(
        token: String?,
        request: R,
    ): Publisher<Authentication> =
        Mono
            .from(validate(token!!, request))
            .map {
                if (daplaLabAudience in it.jwtClaimsSet.getStringListClaim(Claims.AUDIENCE)) {
                    return@map Roles.VARIABLE_OWNER.name
                } else {
                    Roles.VARIABLE_CONSUMER.name
                }
            }.map { Authentication.build("", listOf(it), mapOf()) }

    override fun validate(
        token: String?,
        request: R,
    ): Publisher<JWT> {
        logger.info("Validating for token for $request")
        return Mono.just(jsonWebTokenParser.parse(token).get())
    }
}
