package no.ssb.metadata.vardef.security

import com.nimbusds.jwt.JWT
import com.nimbusds.jwt.JWTClaimsSet
import io.micronaut.context.annotation.Property
import io.micronaut.http.HttpRequest
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.token.jwt.validator.JsonWebTokenParser
import io.micronaut.security.token.jwt.validator.ReactiveJsonWebTokenValidator
import jakarta.inject.Inject
import no.ssb.metadata.vardef.constants.ACTIVE_GROUP
import no.ssb.metadata.vardef.constants.SSB_EMAIL
import no.ssb.metadata.vardef.exceptions.InvalidActiveGroupException
import no.ssb.metadata.vardef.integrations.dapla.services.DaplaTeamService
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono

class LabIdTokenValidator<R : HttpRequest<*>> : ReactiveJsonWebTokenValidator<JWT, R> {
    private val logger = LoggerFactory.getLogger(LabIdTokenValidator::class.java)

    @Property(name = "micronaut.auth.issuers.labid")
    lateinit var allowedIssuers: List<String>

    @Property(name = "micronaut.security.token.jwt.claims-validators.audience")
    private lateinit var allowedAudiences: Set<String>

    @Property(name = "micronaut.security.token.jwt.claims.keys.labid.dapla-group")
    private lateinit var activeGroupClaim: String

    @Property(name = "micronaut.security.token.jwt.claims.keys.labid.dapla-groups")
    private lateinit var daplaGroupsClaim: String

    @Inject
    private lateinit var jsonWebTokenParser: JsonWebTokenParser<JWT>

    private fun getDaplaGroups(claimsSet: JWTClaimsSet): List<String> =
        claimsSet
            .getStringListClaim(daplaGroupsClaim)
            ?: emptyList()

    private fun getActiveGroup(claimsSet: JWTClaimsSet): String? = claimsSet.getStringClaim(activeGroupClaim)

    private fun getUsername(claimsSet: JWTClaimsSet): String? = claimsSet.subject?.plus(SSB_EMAIL)

    /**
     * @return `true` if the principal can be assigned the [VARIABLE_OWNER] role.
     */
    private fun isVariableOwner(claimsSet: JWTClaimsSet): Boolean =
        activeGroupClaim in claimsSet.claims &&
            daplaGroupsClaim in claimsSet.claims &&
            getDaplaGroups(claimsSet).isNotEmpty()

    /**
     * @return `true` if the principal can be assigned the [VARIABLE_CREATOR] role.
     */
    private fun isVariableCreator(
        activeGroup: String?,
        claimsSet: JWTClaimsSet,
    ): Boolean =
        activeGroupClaim in claimsSet.claims &&
            daplaGroupsClaim in claimsSet.claims &&
            getDaplaGroups(claimsSet).isNotEmpty() &&
            activeGroup != null &&
            DaplaTeamService.isDevelopers(activeGroup)

    /**
     * Assign roles
     *
     * The roles are assigned based on claims in the token.
     *
     * By default, authenticated users receive the [VARIABLE_CONSUMER] role. If they fulfill the
     * requirements then they receive other roles in addition.
     *
     * @param claimsSet the claims from the JWT token
     * @return the set of applicable roles.
     * @throws InvalidActiveGroupException
     */
    private fun assignRoles(claimsSet: JWTClaimsSet): Set<String> {
        // If we arrive here the principal is authenticated. Give all principals a default role.

        val roles = mutableSetOf(VARIABLE_CONSUMER)
        val username = getUsername(claimsSet)
        val activeGroup = getActiveGroup(claimsSet)

        logger.debug("Assigning roles for user=$username activeGroup=$activeGroup")

        if (isVariableOwner(claimsSet)) {
            roles.add(VARIABLE_OWNER)
            logger.debug("User=$username assigned role=$VARIABLE_OWNER")
        }
        if (isVariableCreator(activeGroup, claimsSet)) {
            roles.add(VARIABLE_CREATOR)
            logger.debug("User=$username assigned role=$VARIABLE_CREATOR")
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
                logger.info("Validated LabID token for user=$username")
                val attributes = it.claims.toMutableMap()
                attributes[ACTIVE_GROUP] = it.getStringClaim(activeGroupClaim)
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
