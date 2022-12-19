plugins {
    java
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(16))
    }
}

dependencies {
    compileOnly(project(":emojis-plugin"))
    compileOnly("io.papermc.paper:paper:1.17-R0.1-SNAPSHOT")
    compileOnly("net.kyori:adventure-text-minimessage:4.12.0")
}