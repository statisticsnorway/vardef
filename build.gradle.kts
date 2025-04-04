plugins {
    alias(libs.plugins.jvm)
    alias(libs.plugins.allopen)
    alias(libs.plugins.ksp)
    alias(libs.plugins.micronaut.application)
    alias(libs.plugins.micronaut.test.resources)
    alias(libs.plugins.micronaut.aot)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.jib)
    alias(libs.plugins.cyclonedx)
    id("jacoco")
}

version = "0.1.4"
group = "no.ssb.metadata.vardef"

val kotlinVersion = project.properties["kotlinVersion"]
repositories {
    mavenCentral()
}

dependencies {
    ksp(libs.micronaut.data.document.processor)
    ksp(libs.micronaut.http.validation)
    ksp(libs.micronaut.openapi)
    ksp(libs.micronaut.serde.processor)
    ksp(libs.micronaut.validation.processor)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.micronaut.mongo.reactive)
    implementation(libs.micronaut.cache.caffeine)
    implementation(libs.micronaut.data.mongodb)
    implementation(libs.micronaut.http.client)
    implementation(libs.micronaut.management)
    implementation(libs.micronaut.micrometer.registry.prometheus)
    implementation(libs.micronaut.problem.json)
    implementation(libs.micronaut.serde.jackson)
    implementation(libs.micronaut.jackson.xml)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlin.stdlib.jdk8)
    implementation(libs.kotlinx.coroutines.reactor)
    implementation(libs.nanoid)
    implementation(libs.micronaut.security.jwt)
    implementation(libs.reactor)
    implementation(libs.logstash.logback.encoder)
    implementation(libs.logback.classic)
    implementation(libs.mongock.standalone)
    implementation(libs.mongock.mongodb.reactive.driver)
    compileOnly(libs.micronaut.openapi.annotations)
    runtimeOnly(libs.jackson.module.kotlin)
    runtimeOnly(libs.snakeyaml)
    testImplementation(libs.assertj.core)
    testImplementation(libs.micronaut.test.rest.assured)
    testImplementation(libs.mockk)
    testImplementation(libs.junit.jupiter.params)
    testImplementation(libs.json)
    testImplementation(libs.logback.classic)
    aotPlugins(platform(libs.micronaut.platform))
}

application {
    mainClass = "no.ssb.metadata.vardef.ApplicationKt"
}
kotlin { jvmToolchain(21) }

graalvmNative.toolchainDetection = false
micronaut {
    runtime("netty")
    testRuntime("junit5")
    importMicronautPlatform.set(true)
    processing {
        incremental(true)
        annotations("no.ssb.metadata.vardef.*")
    }
    aot {
        // Please review carefully the optimizations enabled below
        // Check https://micronaut-projects.github.io/micronaut-aot/latest/guide/ for more details
        optimizeServiceLoading = false
        convertYamlToJava = false
        precomputeOperations = true
        cacheEnvironment = true
        optimizeClassLoading = true
        deduceEnvironment = true
        optimizeNetty = true
        replaceLogbackXml = true
    }
}

tasks.cyclonedxBom {
    setIncludeConfigs(listOf("runtimeClasspath"))
    setProjectType("application")
}

jib {
    from {
        image = "gcr.io/distroless/java21-debian12@sha256:70e8a4991b6e37cb1eb8eac3b717ed0d68407d1150cf30235d50cd33b2c44f7e"
        platforms {
            platform {
                architecture = "amd64"
                os = "linux"
            }
            platform {
                architecture = "arm64"
                os = "linux"
            }
        }
    }
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "no.ssb.metadata.vardef.ApplicationKt"
    }

    // To avoid the duplicate handling strategy error
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    // To add all the dependencies otherwise a "NoClassDefFoundError" error
    from(sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath
            .get()
            .filter { it.name.endsWith("jar") }
            .map { zipTree(it) }
    })
}

tasks.named<io.micronaut.gradle.docker.NativeImageDockerfile>("dockerfileNative") {
    jdkVersion = "21"
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed", "standardOut", "standardError")
    }
    finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
}

tasks.withType<JacocoReport> {
    dependsOn(tasks.withType<Test>())
    reports {
        xml.required = true
    }
}

val versionFile = file("build.gradle.kts")

fun bumpVersion(type: String) {
    val versionRegex = """version\s*=\s*"(\d+)\.(\d+)\.(\d+)"""".toRegex()
    val content = versionFile.readText()

    val updatedContent =
        versionRegex.replace(content) { matchResult ->
            val (major, minor, patch) = matchResult.destructured
            val newVersion =
                when (type) {
                    "major" -> "${major.toInt() + 1}.0.0"
                    "minor" -> "$major.${minor.toInt() + 1}.0"
                    "patch" -> "$major.$minor.${patch.toInt() + 1}"
                    else -> throw IllegalArgumentException("Invalid version type: $type")
                }
            """version = "$newVersion""""
        }

    versionFile.writeText(updatedContent)
    println("Successfully updated version")
}

tasks.register("versionMajor") {
    group = "versioning"
    description = "Bump the major version"
    doLast {
        bumpVersion("major")
    }
}

tasks.register("versionMinor") {
    group = "versioning"
    description = "Bump the minor version"
    doLast {
        bumpVersion("minor")
    }
}

tasks.register("versionPatch") {
    group = "versioning"
    description = "Bump the patch version"
    doLast {
        bumpVersion("patch")
    }
}
