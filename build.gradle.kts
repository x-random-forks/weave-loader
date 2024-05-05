plugins {
    id("config-kotlin")
    id("config-shade")
    `maven-publish`
}
// TODO: proper publishing
// TODO: api jar
// TODO: minecraft submodule

repositories {
    maven("https://repo.weavemc.dev/releases")
    maven("https://maven.fabricmc.net/")
    mavenCentral()
}

dependencies {
    shade(libs.kxSer)
    shade(libs.bundles.asm)
    shade(libs.weaveInternals)
    shade(libs.mappingsUtil)
    shade(libs.mixin) {
        exclude(group = "com.google.guava")
        exclude(group = "com.google.code.gson")
    }
}

tasks.jar {
    manifest.attributes(
        "Premain-Class" to "net.weavemc.loader.impl.bootstrap.AgentKt",
        "Main-Class" to "net.weavemc.loader.impl.bootstrap.AgentKt",
        "Can-Retransform-Classes" to "true",
    )
}

publishing {
    repositories {
        maven("https://repo.weavemc.dev/releases") {
            name = "WeaveMC"
            credentials(PasswordCredentials::class)
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }

    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            groupId = "net.weavemc"
            artifactId = "loader"
            version = "${project.version}"
        }
    }
}
