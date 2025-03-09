plugins {
    application
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
}

dependencies {
    implementation(libs.io.javalin.javalin)
    implementation(libs.com.fasterxml.jackson.core.jackson.databind)
    implementation(libs.org.openpnp.opencv)
    implementation(libs.org.slf4j.slf4j.simple)
    testImplementation(libs.org.junit.jupiter.junit.jupiter)
}

group = "sh.grover.dcubed"
version = "1.0-SNAPSHOT"
description = "dCubed"
java.sourceCompatibility = JavaVersion.toVersion("23")

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc>() {
    options.encoding = "UTF-8"
}

application {
    mainClass.set("sh.grover.dcubed.Main")
}
