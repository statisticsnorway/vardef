package no.ssb.metadata

import io.micronaut.runtime.Micronaut.run
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.extensions.Extension
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty
import io.swagger.v3.oas.annotations.info.Contact
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.info.License

@OpenAPIDefinition(
    info = Info(
            title = "Variable Definitions",
            description = "",
            version = "0.1",
            license = License(name = "MIT License", url = "https://opensource.org/licenses/MIT"),
            contact = Contact(email = "mmw@ssb.no"),
            extensions = arrayOf(
                Extension(properties = arrayOf(
                    ExtensionProperty(name = "x-audience", value = "external-public")
                ))
            )
    )
)
object Api {
}
fun main(args: Array<String>) {
	run(*args)
}

