package no.ssb.metadata.vardef.utils

import com.nimbusds.jose.Header
import com.nimbusds.jose.util.Base64URL
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT

/**
 * Build a JWT token based on the format from Dapla Lab
 */
class JwtTokenHelper {
    companion object {
        fun jwtTokenSigned(
            audienceClaim: List<String> =
                listOf(
                    "onyxia-api",
                    "broker",
                    "account",
                ),
            daplaTeams: List<String> =
                listOf(
                    "play-foeniks-a",
                    "play-enhjoern-a",
                ),
            daplaGroups: List<String> =
                listOf(
                    "play-enhjoern-a-developers",
                    "play-foeniks-a-developers",
                ),
        ) = SignedJWT(
            header.toBase64URL(),
            claims(audienceClaim, daplaTeams, daplaGroups).toPayload().toBase64URL(),
            signature,
        )

        private val signature =
            Base64URL(
                "ZnTL_4a47t27AmMx-YY5g0qA9HkgjmU7YCxyl7fHdp3mlxGEIAb3vlKE1pifWu9w9Y3fwJwCul2i0UP_KVj7koA7z8C" +
                    "WsBeaMajSwe6UWNemp3JU-SITm6qbGudg4DLlXvq0b-iSNoQq2nZ4kAua3HyRhFjxuEEOzzZEVJHIJYyHgt-QAGuEK2mBo" +
                    "SsObfSqc17Bj4tY_bo5_WRGKSbHzUAw1Km8Xb44GB1j_oz50zk7BMxrItyLEnrKqMd5gzkABZcWd1REfwz58z4mnRKkoEB" +
                    "TcofKXfpAyOL5UfvsvMLVDfeZM5f6nGfFGjp1jztkxFULSt92VNmCPD0ROu_OCA",
            )

        private val header =
            Header.parse(
                """
                {
                  "alg": "RS256",
                  "typ": "JWT",
                  "kid": "qqakWqCUl-slZUT7vEUze1Jf10vWjqEmftTcH7AsBUs"
                }
                """.trimIndent(),
            )

        private fun claims(
            audienceClaim: List<String>,
            daplaTeams: List<String>,
            daplaGroups: List<String>,
        ) = JWTClaimsSet.parse(
            """
            {
            "exp": 1726609866,
            "iat": 1726573866,
            "auth_time": 1726556500,
            "jti": "dc6c5c13-a3ff-42e1-b11c-d93233725ace",
            "iss": "https://auth.ssb.no/realms/ssb",
            "aud": $audienceClaim,
            "sub": "d7532b1f-d5aa-43c1-acd1-ed12d4020455",
            "typ": "Bearer",
            "azp": "onyxia-api",
            "session_state": "43d9f23c-2d2f-4d3b-b519-7cf655ff8230",
            "acr": "0",
            "allowed-origins": [
              "https://lab.dapla.ssb.no"
            ],
            "realm_access": {
              "roles": [
                "offline_access",
                "uma_authorization",
                "default-roles-ssb"
              ]
            },
            "resource_access": {
              "broker": {
                "roles": [
                  "read-token"
                ]
              },
              "account": {
                "roles": [
                  "manage-account",
                  "manage-account-links",
                  "view-profile"
                ]
              }
            },
            "scope": "openid profile email",
            "sid": "43d9f23c-2d2f-4d3b-b519-7cf655ff8230",
            "email_verified": true,
            "dapla": {
              "teams": $daplaTeams,
              "groups": $daplaGroups
            },
            "name": "Ola Nordmann",
            "short_username": "ssb-ano",
            "preferred_username": "ano@ssb.no",
            "given_name": "Ola",
            "family_name": "Nordmann",
            "email": "ano@ssb.no"
                  }
            """.trimIndent(),
        )
    }
}
