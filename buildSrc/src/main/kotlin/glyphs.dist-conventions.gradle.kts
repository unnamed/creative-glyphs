plugins {
    id("glyphs.java-conventions")
    id("com.github.johnrengelman.shadow")
}

tasks {
    processResources {
        filesMatching("**plugin.yml") {
            expand("project" to project)
        }
    }
}