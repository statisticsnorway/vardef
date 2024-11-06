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
import no.ssb.metadata.vardef.constants.ACTIVE_GROUP
import no.ssb.metadata.vardef.exceptions.InvalidActiveGroupException
import no.ssb.metadata.vardef.services.DaplaTeamService
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono

class VardefTokenValidator<R : HttpRequest<*>> : ReactiveJsonWebTokenValidator<JWT, R> {
    private val logger = LoggerFactory.getLogger(VardefTokenValidator::class.java)

    @Property(name = "micronaut.security.token.jwt.claims.values.dapla-lab-audience")
    private lateinit var daplaLabAudience: String

    @Property(name = "micronaut.security.token.jwt.claims.keys.dapla")
    private lateinit var daplaClaim: String

    @Property(name = "micronaut.security.token.jwt.claims.keys.dapla-groups")
    private lateinit var daplaGroupsClaim: String

    @Property(name = "micronaut.security.token.jwt.claims.keys.username")
    private lateinit var usernameClaim: String

    @Inject
    private lateinit var jsonWebTokenParser: JsonWebTokenParser<JWT>

    @Suppress("UNCHECKED_CAST")
    private fun getDaplaGroups(token: JWT) =
        token
            .jwtClaimsSet
            .getJSONObjectClaim(daplaClaim)[daplaGroupsClaim]
            as? List<String> ?: emptyList()

    /**
     * @return `true` if the principal has specified a group which is not present in their token.
     */
    private fun activeGroupSpoofed(
        activeGroup: String,
        token: JWT,
    ): Boolean = activeGroup !in getDaplaGroups(token)

    /**
     * @return `true` if the principal can be assigned the [VARIABLE_OWNER] role.
     */
    private fun isVariableOwner(claimsSet: JWTClaimsSet): Boolean = daplaLabAudience in claimsSet.getStringListClaim(Claims.AUDIENCE)

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
        ACTIVE_GROUP in request.parameters &&
            daplaClaim in token.jwtClaimsSet.claims &&
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
        if (tokenAndRequestContainExpectedFields(token, request)) {
            val activeGroup = request.parameters.get(ACTIVE_GROUP)
            if (activeGroupSpoofed(activeGroup, token)) {
                throw InvalidActiveGroupException("The specified active_group is not present in the token")
            }
            if (isVariableOwner(claimsSet)) roles.add(VARIABLE_OWNER)
            if (isVariableCreator(activeGroup, claimsSet)) roles.add(VARIABLE_CREATOR)
        }

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
            .map {
                Authentication.build(
                    it.jwtClaimsSet.getStringClaim(usernameClaim),
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
            .onErrorResume { Mono.empty() }
}
