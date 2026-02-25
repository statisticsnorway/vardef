package no.ssb.metadata.vardef.integrations.dapla.filters

import io.micronaut.http.HttpResponse
import io.micronaut.http.MutableHttpRequest
import io.micronaut.http.annotation.Filter
import io.micronaut.http.filter.ClientFilterChain
import io.micronaut.http.filter.HttpClientFilter
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory

@Filter(serviceId = ["dapla-api"])
class DaplaClientLoggingFilter : HttpClientFilter {
    private val log = LoggerFactory.getLogger(DaplaClientLoggingFilter::class.java)

    override fun doFilter(
        request: MutableHttpRequest<*>,
        chain: ClientFilterChain,
    ): Publisher<out HttpResponse<*>> {
        log.info("Outgoing request: {} {}", request.method, request.uri)

        return chain.proceed(request)
    }
}
