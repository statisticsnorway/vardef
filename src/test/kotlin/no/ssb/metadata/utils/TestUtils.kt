package no.ssb.metadata.utils

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

fun removeJsonField(mappedJson: String, fieldToRemove: String): String {
    val mapper = jacksonObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)

    val rootNode: JsonNode = mapper.readTree(mappedJson)

    if (rootNode is ObjectNode) {
        rootNode.remove(fieldToRemove)
    }

    return mapper.writeValueAsString(rootNode)
}

