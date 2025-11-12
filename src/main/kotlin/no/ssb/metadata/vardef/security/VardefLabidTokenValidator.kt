package no.ssb.metadata.vardef.security

import com.nimbusds.jwt.JWT
import com.nimbusds.jwt.JWTClaimsSet
import io.micronaut.context.annotation.Property
import io.micronaut.http.HttpRequest
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.token.Claims
import io.micronaut.security.token.jwt.validator.JsonWebTokenParser
import io.micronaut.security.token.jwt.validator.ReactiveJsonWebTokenValidator
import jakarta.inject.Inject
import no.ssb.metadata.vardef.constants.SSB_EMAIL
import no.ssb.metadata.vardef.exceptions.InvalidActiveGroupException
import no.ssb.metadata.vardef.integrations.dapla.services.DaplaTeamService
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono

class VardefLabidTokenValidator<R : HttpRequest<*>> : ReactiveJsonWebTokenValidator<JWT, R> {
    private val logger = LoggerFactory.getLogger(VardefLabidTokenValidator::class.java)

    @Property(name = "micronaut.auth.issuers.labid")
    lateinit var allowedIssuers: List<String>

    @Property(name = "micronaut.security.token.jwt.claims.values.allowed-audiences")
    private lateinit var allowedAudiences: Set<String>

    @Property(name = "micronaut.security.token.jwt.claims.keys.labid-dapla-group")
    private lateinit var activeGroupClaim: String

    @Property(name = "micronaut.security.token.jwt.claims.keys.labid-dapla-groups")
    private lateinit var daplaGroupsClaim: String

    @Property(name = "micronaut.security.token.jwt.claims.keys.labid-username")
    private lateinit var usernameClaim: String

    @Property(name = "micronaut.security.token.jwt.claims.keys.issuer")
    private lateinit var issuerClaim: String

    @Inject
    private lateinit var jsonWebTokenParser: JsonWebTokenParser<JWT>

    @Suppress("UNCHECKED_CAST")
    private fun getDaplaGroups(token: JWT): List<String> =
        token
            .jwtClaimsSet
            .getClaim(daplaGroupsClaim)
            as? List<String> ?: emptyList()

    private fun getActiveGroup(token: JWT): String? = token.jwtClaimsSet.getClaim(activeGroupClaim) as? String

    private fun usernameFromToken(token: JWT): String? = (token.jwtClaimsSet.getClaim(usernameClaim) as? String)?.plus(SSB_EMAIL)

    /**
     * @return `true` if the principal can be assigned the [VARIABLE_OWNER] role.
     */
    private fun isVariableOwner(claimsSet: JWTClaimsSet): Boolean =
        claimsSet.getStringListClaim(Claims.AUDIENCE).any {
            it in allowedAudiences
        }

    /**
     * @return `true` if the principal can be assigned the [VARIABLE_CREATOR] role.
     */
    private fun isVariableCreator(
        activeGroup: String,
        claimsSet: JWTClaimsSet,
    ): Boolean = isVariableOwner(claimsSet) && DaplaTeamService.isDevelopers(activeGroup)

    /**
     * @return `true` the token and request contain the fields necessary to assign roles.
     */
    private fun tokenAndRequestContainExpectedFields(
        token: JWT,
        request: R,
    ): Boolean =
        activeGroupClaim in token.jwtClaimsSet.claims &&
            daplaGroupsClaim in token.jwtClaimsSet.claims &&
            getDaplaGroups(token).isNotEmpty()

    /**
     * Assign roles
     *
     * The roles are assigned based on claims in the token and the `active_group` query parameter.
     *
     * The token is expected to have claims added by `oidc-dapla-userinfo-mapper` with the
     * `nested_teams` config set to `true`. Ref <https://github.com/statisticsnorway/keycloak-iac/blob/4fb230adb60412e7a546612fec9e5b9903400825/pkl/GenericClient.pkl#L160>
     *
     * By default, authenticated users receive the [VARIABLE_CONSUMER] role. If they fulfill the
     * requirements then they receive other roles in addition.
     *
     * @param token the parsed JWT token
     * @param request the [HttpRequest]
     * @return the set of applicable roles.
     * @throws InvalidActiveGroupException
     */
    private fun assignRoles(
        token: JWT,
        request: R,
    ): Set<String> {
        // If we arrive here the principal is authenticated. Give all principals a default role.

        val roles = mutableSetOf(VARIABLE_CONSUMER)
        val claimsSet = token.jwtClaimsSet
        val username = usernameFromToken(token)
        val activeGroup = getActiveGroup(token)

        logger.debug("Assigning roles for user=$username activeGroup=$activeGroup")

        if (activeGroup == null) {
            logger.debug("No active group claim found for user=$username")
            return roles
        }

        if (tokenAndRequestContainExpectedFields(token, request)) {
            if (isVariableOwner(claimsSet)) {
                roles.add(VARIABLE_OWNER)
                logger.debug("User=$username assigned role=$VARIABLE_OWNER")
            }
            if (isVariableCreator(activeGroup, claimsSet)) {
                roles.add(VARIABLE_CREATOR)
                logger.debug("User=$username assigned role=$VARIABLE_CREATOR")
            }
        } else {
            logger.debug("Token missing expected claims for user=$username")
        }

        logger.info("User=$username assigned roles=$roles")
        return roles.toSet()
    }

    /**
     * Authenticate the principal for the given request.
     *
     * @param token
     * @param request
     * @return an [Authentication] containing the principals username and the assigned roles.
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
            .filter {
                it.jwtClaimsSet.getStringClaim(issuerClaim) in allowedIssuers
            }.map {
                val username = usernameFromToken(it)
                logger.info("Validated LabID token for user=$username")
                Authentication.build(
                    username,
                    assignRoles(it, request),
                    it.jwtClaimsSet.claims,
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
            .doOnError { logger.error("Error parsing token for $request", it) }
            .onErrorResume {
                logger.warn("Token parsing failed for request=$request: ${it.message}")
                Mono.empty()
            }
}
