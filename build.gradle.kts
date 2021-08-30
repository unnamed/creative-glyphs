plugins {
    java
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

group = "team.unnamed"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.codemc.io/repository/nms/")
    maven("https://repo.essentialsx.net/releases/")
    mavenLocal()
}

dependencies {
    val spigot = "io.papermc.paper:paper:1.17.1-R0.1-SNAPSHOT";

    compileOnly(spigot)
    compileOnly("org.jetbrains:annotations:21.0.0")
    implementation("net.kyori:adventure-text-minimessage:4.1.0-SNAPSHOT") {
        exclude(group = "net.kyori", module = "adventure-api")
        exclude(group = "net.kyori", module = "adventure-text-serializer-plain")
        exclude(group = "net.kyori", module = "adventure-text-serializer-gson")
    }

    // You must run the deps.sh script to have this dependency
    compileOnly("me.fixeddev:EzChat:2.5.0")

    compileOnly("net.ess3:EssentialsXChat:2.18.2")

    implementation("team.unnamed.hephaestus:common:0.1.0")

    testImplementation(spigot)
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    options.encoding = Charsets.UTF_8.name()
    options.release.set(8)
}