plugins {
    alias(libs.plugins.jvm)
    alias(libs.plugins.allopen)
    alias(libs.plugins.ksp)
    alias(libs.plugins.micronaut.application)
    alias(libs.plugins.micronaut.test.resources)
    alias(libs.plugins.micronaut.aot)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.jib)

    id("jacoco")
}

version = "0.1"
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
    implementation(libs.micronaut.mongo.sync)
    implementation(libs.micronaut.cache.caffeine)
    implementation(libs.micronaut.data.mongodb)
    implementation(libs.micronaut.http.client)
    implementation(libs.micronaut.management)
    implementation(libs.micronaut.micrometer.registry.prometheus)
    implementation(libs.micronaut.serde.jackson)
    implementation(libs.micronaut.jackson.xml)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlin.stdlib.jdk8)
    implementation(libs.kotlinx.coroutines.reactor)
    implementation(libs.nanoid)
    implementation(libs.micronaut.security.jwt)
    implementation(libs.reactor)
    implementation(libs.logstash.logback.encoder)
    runtimeOnly(libs.logback.classic)
    compileOnly(libs.micronaut.openapi.annotations)
    runtimeOnly(libs.jackson.module.kotlin)
    runtimeOnly(libs.snakeyaml)
    testImplementation(libs.micronaut.http.client)
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
