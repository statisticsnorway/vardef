package no.ssb.metadata.vardef.integrations.dapla.utils

fun loadQuery(fileName: String): String =
    object {}::class.java.classLoader
        .getResource("graphql/$fileName")
        ?.readText()
        ?: throw IllegalStateException("Query file $fileName not found")
