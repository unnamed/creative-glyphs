plugins {
    java
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

group = "team.unnamed"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.codemc.io/repository/nms/")
    mavenLocal()
}

dependencies {
    val spigot = "org.spigotmc:spigot-api:1.16.4-R0.1-SNAPSHOT";

    compileOnly(spigot)
    compileOnly("org.jetbrains:annotations:21.0.0")

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
}