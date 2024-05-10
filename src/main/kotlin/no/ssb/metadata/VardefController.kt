package no.ssb.metadata

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get

@Controller("/vardef")
class VardefController {
    @Get(uri = "/", produces = ["text/plain"])
    fun index(): String {
        return "Example Response"
    }
}
