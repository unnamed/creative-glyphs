plugins {
    java
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.1"
}

repositories {
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://nexus.scarsz.me/content/groups/public/") // DiscordSRV
    maven("https://m2.dv8tion.net/releases") // JDA - Required by DiscordSRV
    maven("https://repo.unnamed.team/repository/unnamed-public/") // creative
}

dependencies {
    val spigot = "org.spigotmc:spigot:1.8.8-R0.1-SNAPSHOT";
    val annotations = "org.jetbrains:annotations:22.0.0";

    // Required libraries
    compileOnly(spigot)
    compileOnly(annotations)
    implementation("team.unnamed:creative-api:0.5.2-SNAPSHOT")

    // Optional plugin hooks
    compileOnly("me.clip:placeholderapi:2.10.10")
    compileOnly(files("../lib/TownyChat-0.91.jar", "../lib/EzChat-2.5.0-with-dependencies.jar"))
    compileOnly("com.discordsrv:discordsrv:1.26.0")

    // Testing
    testImplementation(spigot)
    testImplementation(annotations)
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

bukkit {
    main = "team.unnamed.emojis.EmojisPlugin"
    name = "unemojis"
    version = project.version.toString()
    apiVersion = "1.13"
    description = "Unnamed Team's Emojis Plugin"
    author = "Unnamed Team"
    softDepend = listOf("PlaceholderAPI", "EzChat", "TownyChat", "DiscordSRV")
    commands {
        create("emojis") {
            description = "Main command for the unemojis plugin"
            usage = "/<command> update <id>"
        }
    }
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

    shadowJar {
        from(project.sourceSets.main.get().output)

        val buildTask = project(":emojis-compat-java16").tasks.named("jar")
        dependsOn(buildTask)
        from(zipTree(buildTask.map { out -> out.outputs.files.singleFile }))

        // relocate libraries
        // TODO: Remove when creative-manage is ready
        val pkg = "team.unnamed.emojis.lib"
        relocate("team.unnamed.creative", "$pkg.creative")
        relocate("net.kyori.examination", "$pkg.examination")
        relocate("net.kyori.adventure.key", "$pkg.adventure.key")
    }
}
