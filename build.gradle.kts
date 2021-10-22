import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

repositories {
    mavenCentral()
    maven("https://repo.codemc.io/repository/nms/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    mavenLocal()
}

dependencies {
    val spigot = "io.papermc.paper:paper:1.17-R0.1-SNAPSHOT";
    val annotations = "org.jetbrains:annotations:21.0.0";

    compileOnly(spigot)
    compileOnly(annotations)
    implementation("net.kyori:adventure-text-minimessage:4.1.0-SNAPSHOT") {
        exclude(group = "net.kyori", module = "adventure-api")
        exclude(group = "net.kyori", module = "adventure-text-serializer-plain")
        exclude(group = "net.kyori", module = "adventure-text-serializer-gson")
    }

    // You must run the deps.sh script to have this dependency
    compileOnly("me.fixeddev:EzChat:2.5.0")

    compileOnly("me.clip:placeholderapi:2.10.10")

    testImplementation(spigot)
    testImplementation(annotations)
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks {
    test {
        useJUnitPlatform()
    }

    java {
        toolchain {
            // use java 8 by default
            languageVersion.set(JavaLanguageVersion.of(8))
        }
    }

    register<ShadowJar>("shadowJar16") {
        group = "shadow"
        description = "Builds this project using Java 16, supporting Paper 1.17+ and Minestom"
        archiveClassifier.set("all-java16")
        from(project.sourceSets.main.get().output)
        configurations = listOf(project.configurations.runtimeClasspath.get())
        exclude("META-INF/INDEX.LIST", "META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")

        java {
            toolchain {
                // use java 16 in this case
                languageVersion.set(JavaLanguageVersion.of(16))
            }
        }
    }

    processResources {
        filter<org.apache.tools.ant.filters.ReplaceTokens>(
            "tokens" to mapOf("version" to project.version)
        )
    }
}
