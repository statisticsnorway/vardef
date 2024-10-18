package no.ssb.metadata.vardef.integrations.vardok.services

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.micronaut.context.annotation.Primary
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton
import no.ssb.metadata.vardef.integrations.vardok.models.VardokResponse
import java.io.File

@Primary
@Requires(env = ["test"])
@Singleton
class StaticVarDokService : VarDokService {
    override fun getVarDokItem(id: String): VardokResponse? {
        val xmlFile = File("src/test/resources/vardokFiles/$id.xml")
        val xmlMapper = XmlMapper().registerKotlinModule()
        val varDokResponse: VardokResponse = xmlMapper.readValue(xmlFile, VardokResponse::class.java)
        return varDokResponse
    }

    override fun getVardokByIdAndLanguage(
        id: String,
        language: String,
    ): VardokResponse? {
        val xmlFile = File("src/test/resources/vardokFiles/${id}$language.xml")
        val xmlMapper = XmlMapper().registerKotlinModule()
        val varDokResponse: VardokResponse = xmlMapper.readValue(xmlFile, VardokResponse::class.java)
        return varDokResponse
    }
}
