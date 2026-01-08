package no.ssb.metadata.vardef.security

import com.nimbusds.jwt.JWT
import com.nimbusds.jwt.JWTClaimsSet
import io.micronaut.context.annotation.Property
import io.micronaut.http.HttpRequest
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.token.jwt.validator.JsonWebTokenParser
import io.micronaut.security.token.jwt.validator.ReactiveJsonWebTokenValidator
import jakarta.inject.Inject
import no.ssb.metadata.vardef.constants.SSB_EMAIL
import no.ssb.metadata.vardef.exceptions.InvalidActiveGroupException
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono

class KeycloakTokenValidator<R : HttpRequest<*>> : ReactiveJsonWebTokenValidator<JWT, R> {
    private val logger = LoggerFactory.getLogger(KeycloakTokenValidator::class.java)

    @Property(name = "micronaut.auth.issuers.keycloak")
    lateinit var allowedIssuers: List<String>

    @Property(name = "micronaut.security.token.jwt.claims-validators.audience")
    private lateinit var allowedAudiences: Set<String>

    @Property(name = "micronaut.security.token.jwt.claims.keys.keycloak.dapla")
    private lateinit var daplaClaim: String

    @Property(name = "micronaut.security.token.jwt.claims.keys.keycloak.dapla-groups")
    private lateinit var groupsClaim: String

    @Inject
    private lateinit var jsonWebTokenParser: JsonWebTokenParser<JWT>

    @Suppress("UNCHECKED_CAST")
    private fun getDaplaGroups(claimsSet: JWTClaimsSet): List<String> =
        claimsSet
            .getJSONObjectClaim(daplaClaim)[groupsClaim]
            as? List<String> ?: emptyList()

    private fun getUsername(claims: JWTClaimsSet): String? = claims.subject?.plus(SSB_EMAIL)

    /**
     * @return `true` if the principal can be assigned the [VARIABLE_OWNER] role.
     */
    private fun isVariableOwner(claimsSet: JWTClaimsSet): Boolean =
        daplaClaim in claimsSet.claims &&
            getDaplaGroups(claimsSet).isNotEmpty()

    /**
     * Assign roles
     *
     * The roles are assigned based on claims in the token.
     *
     * The token is expected to have claims added by `oidc-dapla-userinfo-mapper` with the
     * `nested_teams` config set to `false`. Ref <https://github.com/statisticsnorway/keycloak-iac/blob/0c3362d7d31b9d34780cd88492c5fcd963fc4ef2/pkl/GenericClient.pkl#L182>
     *
     * By default, authenticated users receive the [VARIABLE_CONSUMER] role. If they fulfill the
     * requirements then they receive other roles in addition.
     *
     * @param claimsSet the parsed JWT token
     * @return the set of applicable roles.
     * @throws InvalidActiveGroupException
     */
    private fun assignRoles(claimsSet: JWTClaimsSet): Set<String> {
        // If we arrive here the principal is authenticated. Give all principals a default role.
        val roles = mutableSetOf(VARIABLE_CONSUMER)
        val username = getUsername(claimsSet)

        logger.debug("Assigning roles for user=$username")

        if (isVariableOwner(claimsSet)) {
            roles.add(VARIABLE_OWNER)
            logger.debug("User=$username assigned role=$VARIABLE_OWNER")
        }

        logger.info("User=$username assigned roles=$roles")
        return roles.toSet()
    }

    /**
     * Authenticate the principal for the given request.
     *
     * @param token
     * @param request
     * @return an [Authentication] containing the principals username, the assigned roles, the claims, and the active group.
     */
    override fun validateToken(
        token: String?,
        request: R,
    ): Publisher<Authentication> {
        if (token == null) {
            return Mono.empty()
        }
        return Mono
            .from(validate(token, request))
            .map { it.jwtClaimsSet }
            .filter {
                it.issuer in allowedIssuers &&
                    it.audience.any { aud ->
                        aud in allowedAudiences
                    } &&
                    it.subject != null
            }.map {
                val username = getUsername(it)
                logger.info("Validated Keycloak token for user=$username")
                val attributes = it.claims.toMutableMap()
                Authentication.build(
                    username,
                    assignRoles(it),
                    attributes,
                )
            }
    }

    /**
     *  Parse the JWT token
     */
    override fun validate(
        token: String?,
        request: R,
    ): Publisher<JWT> =
        Mono
            .just(jsonWebTokenParser.parse(token))
            .filter { it.isPresent }
            .map { it.get() }
            .onErrorResume {
                logger.warn("Token parsing failed for request=$request: ${it.message}")
                Mono.empty()
            }
}
