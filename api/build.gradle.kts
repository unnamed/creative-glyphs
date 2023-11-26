plugins {
    id("glyphs.java-conventions")
}

repositories {
    maven("https://repo.unnamed.team/repository/unnamed-public/")
}

dependencies {
    // Aho-Corasick implementation
    api("org.ahocorasick:ahocorasick:0.6.3")

    compileOnlyApi(libs.annotations)
    compileOnlyApi(libs.gson)

    // Adventure!
    compileOnlyApi(libs.adventure.api)

    // Creative!
    compileOnlyApi(libs.creative.api)
    compileOnlyApi(libs.creative.central.api)

    // Include compileOnly libs in test classpath
    testImplementation(libs.adventure.api)
    testImplementation(libs.creative.api)
    testImplementation(libs.creative.central.api)
}