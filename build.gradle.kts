import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project

plugins {
    kotlin("jvm") version "2.0.0"
    `maven-publish`
}

group = "top.birthcat"
version = "1.0.0"

kotlin {
    compilerOptions.jvmTarget = JvmTarget.JVM_22
}

repositories {
    mavenCentral()
}

val kotlinSource  = tasks.create<Jar>("kotlinSource") {
    archiveClassifier = "sources"
    from(sourceSets["main"].allSource)
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/sheng-ri/ktor-session-redis")
            credentials {
                username = "sheng-ri"
                password = (project.findProperty("gpr.key") ?: System.getenv("TOKEN")) as String
            }
        }
    }
    publications  {
        register<MavenPublication>("gpr") {
            from(components["kotlin"])
            artifact(kotlinSource)
        }
    }
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-sessions-jvm:$ktor_version")
    testImplementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
    testImplementation("ch.qos.logback:logback-classic:$logback_version")
    testImplementation("io.ktor:ktor-server-tests-jvm:$ktor_version")
    testImplementation(kotlin("test"))

    testImplementation("io.lettuce:lettuce-core:6.3.2.RELEASE")
}
