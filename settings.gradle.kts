rootProject.name = "emojis-parent"

includePrefixed("plugin")

fun includePrefixed(name: String) {
    include("emojis-$name")
    project(":emojis-$name").projectDir = file(name)
}