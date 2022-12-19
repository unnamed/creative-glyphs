rootProject.name = "emojis-parent"

includePrefixed("plugin")
includePrefixed("compat-java16")

fun includePrefixed(name: String) {
    include("emojis-$name")
    project(":emojis-$name").projectDir = file(name)
}