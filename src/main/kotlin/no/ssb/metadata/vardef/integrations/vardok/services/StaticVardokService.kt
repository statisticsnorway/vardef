package no.ssb.metadata.vardef.integrations.vardok.services

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.micronaut.context.annotation.Primary
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton
import no.ssb.metadata.vardef.integrations.vardok.models.VardokResponse
import java.io.File

@Primary
@Requires(env = ["test"], notEnv = ["integration-test"])
@Singleton
class StaticVardokService : VardokService {
    private val xmlMapper = XmlMapper().registerKotlinModule()

    override fun getVardokItem(id: String): VardokResponse? {
        val xmlFile = File("src/test/resources/vardokFiles/$id.xml")
        val varDokResponse: VardokResponse = xmlMapper.readValue(xmlFile, VardokResponse::class.java)
        return varDokResponse
    }

    override fun getVardokByIdAndLanguage(
        id: String,
        language: String,
    ): VardokResponse? {
        val xmlFile = File("src/test/resources/vardokFiles/${id}$language.xml")
        val varDokResponse: VardokResponse = xmlMapper.readValue(xmlFile, VardokResponse::class.java)
        return varDokResponse
    }

    override fun fetchMultipleVardokItemsByLanguage(id: String): MutableMap<String, VardokResponse> {
        val xmlFile = File("src/test/resources/vardokFiles/$id.xml")
        val result: VardokResponse = xmlMapper.readValue(xmlFile, VardokResponse::class.java)

        val responseMap = mutableMapOf<String, VardokResponse>()
        result.let {
            responseMap["nb"] = it
        }
        result.otherLanguages.split(";").filter { it.isNotEmpty() }.forEach { l ->
            getVardokByIdAndLanguage(id, l)?.let { responseMap[l] = it }
        }

        return responseMap
    }
}
