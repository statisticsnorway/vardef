package no.ssb.metadata.vardef.security

import com.nimbusds.jwt.JWT
import io.micronaut.context.annotation.Property
import io.micronaut.http.HttpRequest
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.token.Claims
import io.micronaut.security.token.jwt.validator.JsonWebTokenParser
import io.micronaut.security.token.jwt.validator.ReactiveJsonWebTokenValidator
import jakarta.inject.Inject
import no.ssb.metadata.vardef.constants.ACTIVE_GROUP
import no.ssb.metadata.vardef.exceptions.InvalidActiveGroupException
import no.ssb.metadata.vardef.exceptions.InvalidActiveTeamException
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

    @Property(name = "micronaut.security.token.jwt.claims.keys.dapla-teams")
    private lateinit var daplaTeamsClaim: String

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

    @Suppress("UNCHECKED_CAST")
    private fun getDaplaTeams(token: JWT) =
        token
            .jwtClaimsSet
            .getJSONObjectClaim(daplaClaim)[daplaTeamsClaim]
            as? List<String> ?: emptyList()

    /**
     * Is selected team in token
     * Team is a substring of group
     * @param request
     * @param token
     * @return true if team is in token
     */
    private fun isValidTeam(
        request: R,
        token: JWT,
    ): Boolean {
        val group: String = request.parameters.get(ACTIVE_GROUP)
        return group.substringBeforeLast("-") in getDaplaTeams(token)
    }

    /**
     * Assign roles
     *
     * The roles are assigned based on claims in the token and the `active_group` query parameter.
     *
     * The token is expected to have claims added by `oidc-dapla-userinfo-mapper` with the
     * `nested_teams` config set to `true`. Ref <https://github.com/statisticsnorway/keycloak-iac/blob/4fb230adb60412e7a546612fec9e5b9903400825/pkl/GenericClient.pkl#L160>
     *
     * By default, authenticated users receive the [VARIABLE_CONSUMER] role. If they fulfill the
     * requirements then they receive the [VARIABLE_OWNER] role.
     *
     * @param token the parsed JWT token
     * @param request the [HttpRequest]
     * @return the applicable role.
     * @throws InvalidActiveGroupException
     * @throws InvalidActiveTeamException
     */
    private fun assignRoles(
        token: JWT,
        request: R,
    ): String {
        if (ACTIVE_GROUP in request.parameters && daplaClaim in token.jwtClaimsSet.claims) {
            if (
                request.parameters.get(ACTIVE_GROUP) !in getDaplaGroups(token)
            ) {
                // In this case the user is trying to act on behalf of a group they are not a member
                // of ,so we don't want to continue processing this request.
                throw InvalidActiveGroupException("The specified active_group is not present in the token")
            }
            if (!isValidTeam(request, token)) {
                // Group has no valid team in token and the request is not valid
                throw InvalidActiveTeamException("The specified team is not present in the token")
            }

            if (daplaLabAudience in token.jwtClaimsSet.getStringListClaim(Claims.AUDIENCE)
            ) {
                return VARIABLE_OWNER
            }
        }

        // Default role for authenticated principals
        return VARIABLE_CONSUMER
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
                    listOf(assignRoles(it, request)),
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
    ): Publisher<JWT> {
        logger.info("Validating for token for $request")
        return Mono.just(jsonWebTokenParser.parse(token).get())
    }
}
