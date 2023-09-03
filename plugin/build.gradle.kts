plugins {
    id("glyphs.dist-conventions")
}

repositories {
    mavenLocal()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.codemc.io/repository/nms/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") // PlaceholderAPI
    maven("https://nexus.scarsz.me/content/groups/public/") // DiscordSRV
    maven("https://m2.dv8tion.net/releases") // JDA - Required by DiscordSRV
    maven("https://repo.unnamed.team/repository/unnamed-public/") // creative
    mavenCentral()
}

dependencies {

    implementation(project(":creative-glyphs-api"))

    val serverApi = "io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT";

    // Required libraries
    compileOnly(serverApi)

    // Optional libraries
    compileOnly(libs.adventure.text.minimessage)

    // Optional plugin hooks
    compileOnly("me.clip:placeholderapi:2.10.10")
    compileOnly(files("lib/TownyChat-0.91.jar", "lib/EzChat-3.0.3.jar"))
    compileOnly("com.discordsrv:discordsrv:1.26.0")
    compileOnly("io.github.miniplaceholders:miniplaceholders-api:2.1.0")

    // Testing
    testImplementation(serverApi)
    testImplementation(libs.annotations)
}

tasks {
    shadowJar {
        val pkg = "team.unnamed.creativeglyphs.lib"
        relocate("org.ahocorasick", "$pkg.ahocorasick")
    }
}
