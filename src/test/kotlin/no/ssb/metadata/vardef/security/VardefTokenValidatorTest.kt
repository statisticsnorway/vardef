package no.ssb.metadata.vardef.security

import com.nimbusds.jwt.JWT
import io.micronaut.http.HttpRequest
import io.micronaut.security.authentication.Authentication
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import no.ssb.metadata.vardef.utils.JwtTokenHelper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription

@MicronautTest
class VardefTokenValidatorTest {
    @Inject
    lateinit var vardefTokenValidator: VardefTokenValidator<HttpRequest<*>>

    @Test
    fun `validation experimentation`() {
        val result: Publisher<JWT> =
            vardefTokenValidator.validate(
                JwtTokenHelper.jwtTokenSigned().parsedString,
                HttpRequest.POST("/vardef", ""),
            )

        result.subscribe(
            object : Subscriber<JWT> {
                override fun onSubscribe(subscription: Subscription) {
                    subscription.request(1) // Request one item from the publisher
                }

                override fun onNext(jwt: JWT) {
                    // Handle the JWT object here
                    println("Received JWT token: ${jwt.jwtClaimsSet}")
                }

                override fun onError(t: Throwable) {
                    println("Error occurred: ${t.message}")
                }

                override fun onComplete() {
                    println("Token validation complete.")
                }
            },
        )
    }

    @Test
    fun `verify token comes from dapla lab`() {
        val result = vardefTokenValidator.validateToken(JwtTokenHelper.jwtTokenSigned().parsedString, HttpRequest.POST("/vardef", ""))

        var roles: MutableCollection<String>? = null
        result.subscribe(
            object : Subscriber<Authentication> {
                override fun onSubscribe(subscription: Subscription) {
                    subscription.request(1) // Request one item from the publisher
                }

                override fun onNext(auth: Authentication) {
                    // Handle the JWT object here
                    println("Received auth: ${auth.roles}")
                    roles = auth.roles
                }

                override fun onError(t: Throwable) {
                    println("Error occurred: ${t.message}")
                }

                override fun onComplete() {
                    println("Token validation complete.")
                }
            },
        )
        assertThat(roles).containsExactly("VARIABLE_OWNER")
    }
}
