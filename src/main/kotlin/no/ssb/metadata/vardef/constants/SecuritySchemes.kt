package no.ssb.metadata.vardef.constants

sealed class SecuritySchemes {
    companion object {
        const val KEYCLOAK_TOKEN = "keycloak_token"
        const val LABID_TOKEN = "labid_token"
    }
}
