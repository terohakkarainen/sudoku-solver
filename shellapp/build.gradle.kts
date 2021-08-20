import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.21"
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("com.github.gmazzo.buildconfig") version "3.0.2"
    application
}

group = "fi.thakki.sudokusolver.shellapp"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.2")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.24")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.2")
    implementation(project(":engine"))
}

buildConfig {
    useKotlinOutput()
    packageName("fi.thakki.sudokusolver.shellapp")
    buildConfigField("String", "version", "\"${project.version}\"")
}

tasks {
    withType<Test> {
        useJUnitPlatform()
    }

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }

    application {
        mainClass.set("fi.thakki.sudokusolver.shellapp.SudokuSolverMain")
    }

    shadowJar {
        archiveClassifier.set("") // No "-all" postfix.
    }
}
