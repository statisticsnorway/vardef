package no.ssb.metadata.vardef.utils

import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.util.Base64URL
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import org.json.JSONObject
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date

class KeycloakTokenHelper {
    companion object {
        fun tokenSigned(
            audienceClaim: List<String> =
                listOf(
                    "vardef",
                ),
            daplaGroups: List<String>? =
                listOf(
                    TEST_DEVELOPERS_GROUP,
                    "play-foeniks-a-developers",
                ),
            lifetimeSeconds: Long = 3600,
            includeUsername: Boolean = true,
        ): SignedJWT {
            val now = Instant.now()
            val daplaClaim = JSONObject()
            daplaClaim.put("groups", daplaGroups)
            daplaClaim.put("teams", emptyList<String>())
            daplaClaim.put("section_code", "")
            daplaClaim.put("section_name", "")
            val claims =
                JWTClaimsSet
                    .Builder()
                    .audience(audienceClaim)
                    .issuer("https://auth.ssb.no/realms/ssb")
                    .claim("scope", "all_groups,current_group")
                    .claim("dapla", daplaClaim)
                    .issueTime(Date.from(now))
                    .expirationTime(
                        Date.from(
                            now.plus(
                                lifetimeSeconds,
                                ChronoUnit.SECONDS,
                            ),
                        ),
                    )
            if (includeUsername) {
                claims.subject(LABID_TEST_USER)
            }

            return SignedJWT(
                header.toBase64URL(),
                claims.build().toPayload().toBase64URL(),
                signature,
            )
        }

        private val header =
            JWSHeader.parse(
                """
                {
                  "alg": "RS256",
                  "typ": "JWT",
                  "kid": "keycloak-key"
                }
                """.trimIndent(),
            )

        private val signature =
            Base64URL(
                "dummy_signature_replace_with_real",
            )
    }
}
