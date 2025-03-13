package no.ssb.metadata.vardef.integrations.vardok.services

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.micronaut.context.annotation.Primary
import io.micronaut.context.annotation.Requires
import jakarta.inject.Inject
import jakarta.inject.Singleton
import no.ssb.metadata.vardef.integrations.vardok.models.VardokNotFoundException
import no.ssb.metadata.vardef.integrations.vardok.models.VardokResponse
import no.ssb.metadata.vardef.integrations.vardok.models.VardokVardefIdPair
import no.ssb.metadata.vardef.integrations.vardok.repositories.VardokIdMappingRepository
import no.ssb.metadata.vardef.repositories.VariableDefinitionRepository
import java.io.File
import java.io.FileNotFoundException

@Primary
@Requires(env = ["test"], notEnv = ["integration-test"])
@Singleton
class StaticVardokService(
    private val vardokIdMappingRepository: VardokIdMappingRepository,
) : VardokService {
    private val xmlMapper = XmlMapper().registerKotlinModule()

    @Inject
    lateinit var variableDefinitionRepository: VariableDefinitionRepository

    override fun isDuplicate(name: String): Boolean {
        return variableDefinitionRepository.existsByShortName(name)
    }

    override fun createVardokVardefIdMapping(
        vardokId: String,
        vardefId: String,
    ): VardokVardefIdPair = vardokIdMappingRepository.save(VardokVardefIdPair(vardokId, vardefId))

    override fun getVardefIdByVardokId(vardokId: String): String? = vardokIdMappingRepository.getVardefIdByVardokId(vardokId)

    override fun isAlreadyMigrated(vardokId: String): Boolean = vardokIdMappingRepository.existsByVardokId(vardokId)

    override fun getVardokItem(id: String): VardokResponse {
        try {
            val xmlFile = File("src/test/resources/vardokFiles/$id.xml")
            val varDokResponse: VardokResponse = xmlMapper.readValue(xmlFile, VardokResponse::class.java)
            return varDokResponse
        } catch (e: Exception) {
            if (e is FileNotFoundException) {
                throw VardokNotFoundException("Vardok id $id not found")
            }
            throw e
        }
    }

    override fun getVardokByIdAndLanguage(
        id: String,
        language: String,
    ): VardokResponse {
        try {
            val xmlFile = File("src/test/resources/vardokFiles/${id}$language.xml")
            val varDokResponse: VardokResponse = xmlMapper.readValue(xmlFile, VardokResponse::class.java)
            return varDokResponse
        } catch (e: Exception) {
            if (e is FileNotFoundException) {
                throw (VardokNotFoundException("Id $id in language: $language not found"))
            }
            throw e
        }
    }

    override fun fetchMultipleVardokItemsByLanguage(id: String): MutableMap<String, VardokResponse> {
        val result = getVardokItem(id)
        val responseMap = mutableMapOf<String, VardokResponse>()
        result.let {
            if (result.xmlLang == "nn") {
                responseMap["nn"] = it
            } else {
                responseMap["nb"] = it
            }
        }
        result.otherLanguages.split(";").filter { it.isNotEmpty() }.forEach { l ->
            getVardokByIdAndLanguage(id, l).let { responseMap[l] = it }
        }
        if (result.variable?.dataElementName?.let { isDuplicate(it) } == true) {
            result.variable.dataElementName = VardokService.generateShortName()
        }

        return responseMap
    }
}
