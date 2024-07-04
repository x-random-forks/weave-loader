plugins {
    id("config-kotlin")
    id("config-publish")
}

dependencies {
    api(libs.bundles.asm)
    api(libs.mappings)
    compileOnly(project(":internals"))
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            groupId = "net.weavemc.api"
            artifactId = "common"
            version = "${project.version}"
        }
    }
}