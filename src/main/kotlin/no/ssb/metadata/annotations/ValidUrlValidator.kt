package no.ssb.metadata.annotations

import java.net.MalformedURLException
import java.net.URI


object ValidUrlValidator {
    fun isValid(value: String?): Boolean {

        val regex = "^(https?|ftp)://[^\\s/$.?#].\\S*$".toRegex()

        return !value.isNullOrBlank() && regex.matches(value)
    }
}