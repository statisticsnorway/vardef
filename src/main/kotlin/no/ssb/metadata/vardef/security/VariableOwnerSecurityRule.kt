package no.ssb.metadata.vardef.security

import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.http.HttpRequest
import io.micronaut.http.annotation.PathVariable
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.AbstractSecurityRule
import io.micronaut.security.rules.SecuredAnnotationRule
import io.micronaut.security.rules.SecurityRuleResult
import io.micronaut.security.token.RolesFinder
import io.micronaut.web.router.MethodBasedRouteMatch
import io.micronaut.web.router.RouteAttributes
import io.micronaut.web.router.RouteMatch
import jakarta.inject.Singleton
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.mono
import kotlinx.coroutines.runBlocking
import no.ssb.metadata.vardef.constants.ACTIVE_GROUP
import no.ssb.metadata.vardef.constants.VARIABLE_DEFINITION_ID_PATH_VARIABLE
import no.ssb.metadata.vardef.models.Owner
import no.ssb.metadata.vardef.models.SavedVariableDefinition
import no.ssb.metadata.vardef.services.VariableDefinitionService
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

/**
 * Variable Owner security rule
 *
 * The [VARIABLE_OWNER] role is required on operations where a principal _modifies_ an existing resource. Due to our
 * authentication architecture, the bearer token does not contain information about which concrete resources the
 * principal has access to.
 *
 * Instead, the token lists which _groups_ the principal is a member of. In this class we make use of the [ACTIVE_GROUP]
 * query parameter. This can be trusted because it has already been verified in [VardefLabIdTokenValidator] which provides
 * the [Authentication] and the roles contained within.
 *
 * The primary check that class performs is whether the provided `active_group` is present in the list of groups
 * defined in the [Owner] structure in the [SavedVariableDefinition].
 *
 * @property variableDefinitionService
 * @constructor
 *
 * @param rolesFinder
 */
@Singleton
class VariableOwnerSecurityRule(
    rolesFinder: RolesFinder,
    private val variableDefinitionService: VariableDefinitionService,
) : AbstractSecurityRule<HttpRequest<*>>(rolesFinder) {
    private val logger = LoggerFactory.getLogger(VariableOwnerSecurityRule::class.java)

    /**
     * Get order
     *
     * For correct behaviour, this Rule must run BEFORE [SecuredAnnotationRule]
     */
    override fun getOrder(): Int = SecuredAnnotationRule.ORDER - 50

    /**
     * Does the operation require the [VARIABLE_OWNER] role.
     *
     * Here we're checking whether the operation is annotated with the [Secured] annotation, with [VARIABLE_OWNER]
     * supplied as one of the values.
     *
     * @param routeMatch the [RouteMatch] from Micronaut.
     * @return `true` if the role is required for this operation.
     */
    private fun doesOperationRequireVariableOwnerRole(routeMatch: RouteMatch<*>): Boolean {
        if (routeMatch is MethodBasedRouteMatch<*, *>) {
            val securedAnnotation: AnnotationValue<Secured>? = routeMatch.getAnnotation(Secured::class.java)
            if (securedAnnotation != null) {
                val optionalValue = routeMatch.getValue(Secured::class.java, Array<String>::class.java)
                if (optionalValue.isPresent) {
                    val requiredRoles: List<String> = optionalValue.get().toList()
                    return VARIABLE_OWNER in requiredRoles
                }
            }
        }
        return false
    }

    /**
     * Extract the definition ID.
     *
     * This is found in operations with paths defined in the form "/variable-definitions/{variable-definition-id}".
     * If the path uses another format for the [PathVariable] then we won't be able to extract the definition ID.
     * It's recommended to use the constant [VARIABLE_DEFINITION_ID_PATH_VARIABLE] when defining paths for consistency.
     *
     * This method could throw a [NullPointerException] if the variable is not present. Use of this method should be
     * guarded by checking for the variable's presence.
     *
     * @param routeMatch the [RouteMatch] from Micronaut.
     * @return the requested definition ID
     */
    private fun extractDefinitionIdFromUri(routeMatch: RouteMatch<*>): String =
        routeMatch.variableValues[VARIABLE_DEFINITION_ID_PATH_VARIABLE] as String

    /**
     * Check whether the principal is allowed access to the resource.
     *
     * @param request [HttpRequest]
     * @param authentication [Authentication]
     * @return a [SecurityRuleResult]
     */
    @Suppress("ReactiveStreamsTooLongSameOperatorsChain") // Too little data to cause performance overhead
    override fun check(
        request: HttpRequest<*>?,
        authentication: Authentication?,
    ): Publisher<SecurityRuleResult> {
        if (request == null || authentication == null) return Mono.just(SecurityRuleResult.UNKNOWN)
        return Mono
            .justOrEmpty(RouteAttributes.getRouteMatch(request))
            .filter { doesOperationRequireVariableOwnerRole(it) }
            .filter { VARIABLE_DEFINITION_ID_PATH_VARIABLE in it.variableValues }
            .map { extractDefinitionIdFromUri(it) }
            // The next call is blocking, so we need to run it on another thread
            .publishOn(Schedulers.boundedElastic())
            .map { definitionId ->
                // Get active group from authentication attributes
                val activeGroup = authentication.attributes[ACTIVE_GROUP] as String?
                if (activeGroup == null) {
                    logger.info("No active group found in request or authentication claims. Request: $request")
                    return@map false
                }
                runBlocking {
                    variableDefinitionService
                        .groupIsOwner(activeGroup, definitionId)
                }
            }.handle { isOwner, sink ->
                if (VARIABLE_OWNER !in authentication.roles) {
                    logger.info("Rejected access. Principal does not have $VARIABLE_OWNER role. Request: $request")
                    sink.next(SecurityRuleResult.REJECTED)
                    sink.complete()
                } else {
                    if (isOwner) {
                        logger.info("Allowed access for: $request")
                        sink.next(SecurityRuleResult.ALLOWED)
                        sink.complete()
                    } else {
                        logger.info("Rejected access. Principal does not own the requested resource. Request: $request")
                        sink.next(SecurityRuleResult.REJECTED)
                        sink.complete()
                    }
                }
            }.switchIfEmpty(Mono.just(SecurityRuleResult.UNKNOWN))
            .doOnError { logger.error("Error while authorizing for $VARIABLE_OWNER for $request", it) }
            .onErrorReturn(SecurityRuleResult.UNKNOWN)
    }
}
