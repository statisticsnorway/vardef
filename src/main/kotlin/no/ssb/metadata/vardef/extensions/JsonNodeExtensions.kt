package no.ssb.metadata.vardef.extensions

import io.micronaut.json.tree.JsonNode

fun JsonNode.has(key: String): Boolean = this.entries().find { it.key == key } != null

fun JsonNode.fieldNames(): List<String> = this.entries().map { it.key }
