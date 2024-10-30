package no.ssb.metadata.vardef.security

import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.http.HttpAttributes
import io.micronaut.http.HttpRequest
import io.micronaut.http.uri.UriMatchInfo
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
import no.ssb.metadata.vardef.services.ValidityPeriodsService
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono
import java.util.*

@Singleton
class VariableOwnerSecurityRule(
    rolesFinder: RolesFinder,
    private val validityPeriods: ValidityPeriodsService,
) : AbstractSecurityRule<HttpRequest<*>>(rolesFinder) {
    private val logger = LoggerFactory.getLogger(VariableOwnerSecurityRule::class.java)

    // Run BEFORE the standard SecuredAnnotationRule
    private val order = SecuredAnnotationRule.ORDER - 50

    override fun getOrder(): Int = order

    override fun check(
        request: HttpRequest<*>?,
        authentication: Authentication?,
    ): Publisher<SecurityRuleResult> {
        val routeMatch = request?.getAttribute(HttpAttributes.ROUTE_MATCH, RouteMatch::class.java)?.orElse(null)
        if (routeMatch is MethodBasedRouteMatch<*, *>) {
            val securedAnnotation: AnnotationValue<Secured>? = routeMatch.getAnnotation(Secured::class.java)
            if (securedAnnotation != null) {
                val optionalValue = routeMatch.getValue(Secured::class.java, Array<String>::class.java)
                if (optionalValue.isPresent) {
                    val roles: List<String> = optionalValue.get().toList()
                    val variables = (routeMatch as UriMatchInfo).variableValues
                    if (roles.contains(VARIABLE_OWNER) && VARIABLE_DEFINITION_ID_PATH_VARIABLE in variables.keys) {
                        val definitionId = variables[VARIABLE_DEFINITION_ID_PATH_VARIABLE] as String
                        val patch = validityPeriods.getLatestPatchInLastValidityPeriod(definitionId)
                        return if (routeMatch.variableValues[ACTIVE_GROUP] in patch.owner.groups) {
                            Mono.just(SecurityRuleResult.ALLOWED)
                        } else {
                            Mono.just(SecurityRuleResult.REJECTED)
                        }
                    }
                }
            }
        }
        return Mono.just(SecurityRuleResult.UNKNOWN)
    }
}
