package no.ssb.metadata.vardef.extensions

import io.micronaut.http.client.exceptions.HttpClientResponseException
import org.json.JSONObject

/**
 * Extract the embedded error message from the response body.
 *
 * Expects that the format in the response is the standard Micronaut [io.micronaut.http.hateoas.JsonError]. This means
 * it's unlikely to work for services which aren't implemented in Micronaut.
 *
 * The client throwing the exception should have been created with `errorType=String::class.java`.
 *
 * @return The embedded error message from the service response body.
 */
fun HttpClientResponseException.extractMessageFromJsonError(): String = JSONObject(this.response.getBody(String::class.java).get()).query("/_embedded/errors/0/message").toString()
