plugins {
    kotlin("jvm") version "1.9.21"
}

group = "com.justai"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-core:2.14.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.1")

    implementation("io.ktor:ktor-client-cio-jvm:1.6.8")
    implementation("io.ktor:ktor-client-auth-jvm:1.6.8")
    implementation("io.ktor:ktor-client-core-jvm:1.6.8")

    implementation("com.theokanning.openai-gpt3-java:service:0.18.2")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(18)
}
