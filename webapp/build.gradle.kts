plugins {
    kotlin("jvm") version "1.4.31"
    application
}

group = "fi.thakki.sudokusolver.webapp"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core:1.5.4")
    implementation("io.ktor:ktor-server-netty:1.5.4")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation(project(":engine"))
}

tasks {
    application {
        mainClass.set("fi.thakki.sudokusolver.webapp.ApplicationKt")
    }
}
