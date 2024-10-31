package no.ssb.metadata.vardef.security

import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.http.HttpAttributes
import io.micronaut.http.HttpRequest
import io.micronaut.http.annotation.PathVariable
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.AbstractSecurityRule
import io.micronaut.security.rules.SecuredAnnotationRule
import io.micronaut.security.rules.SecurityRuleResult
import io.micronaut.security.token.RolesFinder
import io.micronaut.web.router.MethodBasedRouteMatch
import io.micronaut.web.router.RouteMatch
import jakarta.inject.Singleton
import no.ssb.metadata.vardef.constants.ACTIVE_GROUP
import no.ssb.metadata.vardef.constants.VARIABLE_DEFINITION_ID_PATH_VARIABLE
import no.ssb.metadata.vardef.services.VariableDefinitionService
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono
import java.util.*

/**
 * Variable owner security rule
 *
 * @property variableDefinitionService
 * @constructor
 *
 * @param rolesFinder
 */
@Singleton
@ExecuteOn(TaskExecutors.BLOCKING)
class VariableOwnerSecurityRule(
    rolesFinder: RolesFinder,
    private val variableDefinitionService: VariableDefinitionService,
) : AbstractSecurityRule<HttpRequest<*>>(rolesFinder) {
    private val logger = LoggerFactory.getLogger(VariableOwnerSecurityRule::class.java)

    // Run BEFORE the standard SecuredAnnotationRule
    private val order = SecuredAnnotationRule.ORDER - 50

    override fun getOrder(): Int = order

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
    private fun getDefinitionId(routeMatch: RouteMatch<*>): String =
        routeMatch.variableValues[VARIABLE_DEFINITION_ID_PATH_VARIABLE] as String

    override fun check(
        request: HttpRequest<*>?,
        authentication: Authentication?,
    ): Publisher<SecurityRuleResult> {
        val routeMatch =
            request
                ?.getAttribute(HttpAttributes.ROUTE_MATCH, RouteMatch::class.java)
                ?.orElse(null)
        if (routeMatch != null &&
            authentication != null &&
            doesOperationRequireVariableOwnerRole(routeMatch) &&
            VARIABLE_DEFINITION_ID_PATH_VARIABLE in routeMatch.variableValues &&
            ACTIVE_GROUP in request.parameters
        ) {
            if (VARIABLE_OWNER !in authentication.roles) {
                logger.info("Rejected access. Principal does not have necessary role. Request: $request")
                return Mono.just(SecurityRuleResult.REJECTED)
            }
            val activeGroup = request.parameters.get(ACTIVE_GROUP) as String
            return if (variableDefinitionService.groupIsOwner(activeGroup, getDefinitionId(routeMatch))) {
                logger.info("Allowed access for: $request")
                Mono.just(SecurityRuleResult.ALLOWED)
            } else {
                logger.info("Rejected access. Principal does not own the requested resource. Request: $request")
                Mono.just(SecurityRuleResult.REJECTED)
            }
        }
        return Mono.just(SecurityRuleResult.UNKNOWN)
    }
}
