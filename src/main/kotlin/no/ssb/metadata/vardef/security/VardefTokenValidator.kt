package no.ssb.metadata.vardef.security

import com.nimbusds.jwt.JWT
import io.micronaut.http.HttpRequest
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.token.jwt.validator.JsonWebTokenParser
import io.micronaut.security.token.jwt.validator.JwtAuthenticationFactory
import io.micronaut.security.token.jwt.validator.ReactiveJsonWebTokenValidator
import jakarta.inject.Inject
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono
import java.util.*

class VardefTokenValidator<R> : ReactiveJsonWebTokenValidator<JWT, R> {
    private val logger = LoggerFactory.getLogger(VardefTokenValidator::class.java)

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
            .map { jwtAuthenticationFactory.createAuthentication(it) }
            .filter { obj: Optional<Authentication> -> obj.isPresent }
            .map { it.get() }

    override fun validate(
        token: String?,
        request: R,
    ): Publisher<JWT> {
        logger.info("Validating for token for $request")
        return Mono.just(jsonWebTokenParser.parse(token).get())
    }
}
