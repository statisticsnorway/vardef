package no.ssb.metadata.klass

import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Header
import io.micronaut.http.client.annotation.Client
import java.net.http.HttpResponse

@Client("https://data.ssb.no/api/klass/v1/classifications")
//@Header(name = "Accept", value = "application/json")
interface ClassificationsClient {

    @Get("/classifications")
    fun fetchData(): HttpResponse<Any>
}