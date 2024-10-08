package no.ssb.metadata.vardef.extensions

import io.micronaut.http.client.exceptions.HttpClientResponseException
import org.json.JSONObject

/**
 * Extract the embedded error message.
 *
 * Expects that the format in the response is created from the standard Micronaut [io.micronaut.http.hateoas.JsonError].
 * This means it's unlikely to work for services which aren't implemented in Micronaut.
 *
 * @return The embedded error message from the response body.
 */
fun HttpClientResponseException.extractMicronautErrorMessage(): String =
    JSONObject(
        this.response.getBody(String::class.java).get(),
    ).query("/_embedded/errors/0/message").toString()
