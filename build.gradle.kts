import groovy.xml.dom.DOMCategory.attributes
import org.jetbrains.kotlin.com.intellij.openapi.vfs.StandardFileSystems.jar

plugins {
    kotlin("jvm") version "1.9.23"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    implementation("org.bouncycastle:bcprov-jdk15on:1.70")
    //implementation("org.bouncycastle:bcprov-jdk15on:1.68")
    implementation("org.xrpl:xrpl4j-client:3.3.0")
    implementation("org.xrpl:xrpl4j-core:3.3.0")
    implementation("org.xerial:sqlite-jdbc:3.39.2.0")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain {
        (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(8))
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

