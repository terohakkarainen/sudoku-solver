import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.21"
    kotlin("plugin.serialization") version "1.5.21"
    id("com.github.gmazzo.buildconfig") version "3.0.2"
    `java-library`
}

group = "fi.thakki.sudokusolver.engine"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")
    implementation("org.yaml:snakeyaml:1.29")
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.2")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.24")
    testImplementation("com.tngtech.archunit:archunit-junit5:0.20.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.2")
}

buildConfig {
    useKotlinOutput()
    packageName("fi.thakki.sudokusolver.engine")
    buildConfigField("String", "version", "\"${project.version}\"")
}

tasks {
    withType<Test> {
        useJUnitPlatform()
    }

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }
}
