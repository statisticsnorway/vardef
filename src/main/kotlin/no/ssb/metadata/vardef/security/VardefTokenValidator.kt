package no.ssb.metadata.vardef.security

import com.nimbusds.jwt.JWT
import io.micronaut.context.annotation.Property
import io.micronaut.http.HttpRequest
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.token.Claims
import io.micronaut.security.token.jwt.validator.JsonWebTokenParser
import io.micronaut.security.token.jwt.validator.ReactiveJsonWebTokenValidator
import jakarta.inject.Inject
import no.ssb.metadata.vardef.exceptions.InvalidActiveGroupException
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono

class VardefTokenValidator<R : HttpRequest<*>> : ReactiveJsonWebTokenValidator<JWT, R> {
    private val logger = LoggerFactory.getLogger(VardefTokenValidator::class.java)

    @Property(name = "dapla.lab.security.audience")
    private lateinit var daplaLabAudience: String

    @Inject
    private lateinit var jsonWebTokenParser: JsonWebTokenParser<JWT>

    @Suppress("UNCHECKED_CAST")
    private fun getDaplaGroups(token: JWT) =
        token.jwtClaimsSet.getJSONObjectClaim("dapla")["groups"] as? List<String>
            ?: emptyList()

    private fun assignRoles(
        token: JWT,
        request: R,
    ): String {
        if ("active_group" in request.parameters) {
            if (
                request.parameters.get("active_group") !in getDaplaGroups(token)
            ) {
                // In this case the user is trying to act on behalf of a group they are not a member
                // of ,so we don't want to continue processing this request.
                throw InvalidActiveGroupException("The specified active_group is not present in the token")
            }

            if (daplaLabAudience in token.jwtClaimsSet.getStringListClaim(Claims.AUDIENCE)
            ) {
                return VARIABLE_OWNER
            }
        }

        // Default role for authenticated principals
        return VARIABLE_CONSUMER
    }

    override fun validateToken(
        token: String?,
        request: R,
    ): Publisher<Authentication> =
        Mono
            .from(validate(token!!, request))
            .map {
                Authentication.build(
                    it.jwtClaimsSet.getStringClaim("preferred_username"),
                    listOf(assignRoles(it, request)),
                    it.jwtClaimsSet.claims,
                )
            }

    override fun validate(
        token: String?,
        request: R,
    ): Publisher<JWT> {
        logger.info("Validating for token for $request")
        return Mono.just(jsonWebTokenParser.parse(token).get())
    }
}
