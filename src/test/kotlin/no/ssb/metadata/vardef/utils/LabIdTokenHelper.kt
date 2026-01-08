package no.ssb.metadata.vardef.utils

import com.nimbusds.jose.Header
import com.nimbusds.jose.util.Base64URL
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import org.json.JSONObject
import java.time.Instant

class LabIdTokenHelper {
    companion object {
        fun tokenSigned(
            audienceClaim: List<String> =
                listOf(
                    "vardef",
                ),
            activeGroup: String = TEST_DEVELOPERS_GROUP,
            daplaGroups: List<String>? =
                listOf(
                    TEST_DEVELOPERS_GROUP,
                    "play-foeniks-a-developers",
                ),
            lifetimeSeconds: Long = 3600,
            includeUsername: Boolean = true,
            includeActiveGroup: Boolean = true,
        ): SignedJWT {
            val now = Instant.now().epochSecond
            val claims =
                JSONObject()
                    .put("aud", audienceClaim)
                    .put("dapla.groups", daplaGroups)
                    .put("exp", now + lifetimeSeconds)
                    .put("iat", now)
                    .put("iss", "https://labid.lab.dapla-external.ssb.no")
                    .put("scope", "all_groups,current_group")

            if (includeUsername) {
                claims.put("sub", LABID_TEST_USER)
            }
            if (includeActiveGroup) {
                claims.put("dapla.group", activeGroup)
            }

            return SignedJWT(
                header.toBase64URL(),
                JWTClaimsSet.parse(claims.toString()).toPayload().toBase64URL(),
                signature,
            )
        }

        private val header =
            Header.parse(
                """
                {
                  "alg": "RS256",
                  "typ": "JWT",
                  "kid": "labid-key"
                }
                """.trimIndent(),
            )

        private val signature =
            Base64URL(
                "dummy_signature_replace_with_real",
            )
    }
}
