package no.ssb.metadata.config

import io.micronaut.context.ApplicationContextBuilder
import io.micronaut.context.ApplicationContextConfigurer
import io.micronaut.context.annotation.ContextConfigurer
import io.micronaut.core.annotation.NonNull
import org.slf4j.LoggerFactory

const val NAIS_CLUSTER_NAME = "NAIS_CLUSTER_NAME"

enum class NaisEnvironment(
    val envName: String,
) {
    TEST("naistest"),
    PROD("naisprod"),
}

@ContextConfigurer
class NaisEnvironmentConfigurer : ApplicationContextConfigurer {
    private val logger = LoggerFactory.getLogger(NaisEnvironmentConfigurer::class.java)

    override fun configure(builder: @NonNull ApplicationContextBuilder) {
        val naisClusterName = System.getenv(NAIS_CLUSTER_NAME)
        builder.apply { parseEnvironment(naisClusterName)?.let { environments(it.envName) } }
    }

    private fun parseEnvironment(naisClusterName: String?): NaisEnvironment? {
        if (naisClusterName == null) return null
        if (naisClusterName.contains("test")) {
            logger.info("Deduced environment ${NaisEnvironment.TEST.envName} from $NAIS_CLUSTER_NAME=$naisClusterName")
            return NaisEnvironment.TEST
        }
        if (naisClusterName.contains("prod")) {
            logger.info("Deduced environment ${NaisEnvironment.PROD.envName} from $NAIS_CLUSTER_NAME=$naisClusterName")
            return NaisEnvironment.PROD
        }
        return null
    }
}
