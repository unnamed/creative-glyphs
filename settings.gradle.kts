rootProject.name = "creative-glyphs"

includePrefixed("api")
includePrefixed("plugin")

fun includePrefixed(name: String) {
    val projectPath = "${rootProject.name}-${name.replace(':', '-')}"
    val projectDir = file(name.replace(':', '/'))

    include(projectPath)
    project(":$projectPath").projectDir = projectDir
}