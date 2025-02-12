import house.greenhouse.greenhouseconfig.gradle.Properties
import house.greenhouse.greenhouseconfig.gradle.Versions

plugins {
    id("conventions.common")
    id("net.neoforged.moddev")
    id("me.modmuss50.mod-publish-plugin")
}

var props = Properties.MODULES["core"]!!
version = "${props.version}+${Versions.MINECRAFT}-common-mojmap"

sourceSets {
    create("generated") {
        resources {
            srcDir("src/generated/resources")
        }
    }
}

neoForge {
    neoFormVersion = Versions.NEOFORM
    parchment {
        minecraftVersion = Versions.MINECRAFT
        mappingsVersion = Versions.PARCHMENT
    }
    addModdingDependenciesTo(sourceSets["test"])

    val at = file("src/main/resources/${props.modId}.cfg")
    if (at.exists())
        setAccessTransformers(at)
    validateAccessTransformers = true
}

dependencies {
    compileOnly("io.github.llamalad7:mixinextras-common:${Versions.MIXIN_EXTRAS}")
    annotationProcessor("io.github.llamalad7:mixinextras-common:${Versions.MIXIN_EXTRAS}")
    compileOnly("net.fabricmc:sponge-mixin:${Versions.FABRIC_MIXIN}")

    testCompileOnly(project(":jsonc"))
    testCompileOnly(project(":toml"))
    testCompileOnly(project(":yaml"))
    testCompileOnly(project(":hocon"))
}

configurations {
    register("commonJava") {
        isCanBeResolved = false
        isCanBeConsumed = true
    }
    register("commonTestJava") {
        isCanBeResolved = false
        isCanBeConsumed = true
    }
    register("commonResources") {
        isCanBeResolved = false
        isCanBeConsumed = true
    }
    register("commonTestResources") {
        isCanBeResolved = false
        isCanBeConsumed = true
    }
}

artifacts {
    add("commonJava", sourceSets["main"].java.sourceDirectories.singleFile)
    add("commonTestJava", sourceSets["test"].java.sourceDirectories.singleFile)
    add("commonResources", sourceSets["main"].resources.sourceDirectories.singleFile)
    add("commonTestResources", sourceSets["test"].resources.sourceDirectories.singleFile)
}

publishMods {
    changelog = rootProject.file("CHANGELOG.md").readText()
    version = "${props.version}+${Versions.MINECRAFT}"
    displayName = "v${props.version} (Minecraft ${Versions.MINECRAFT})"
    type = ALPHA

    github {
        accessToken = providers.environmentVariable("GITHUB_TOKEN")
        repository = Properties.GITHUB_REPO
        tagName = "${props.version}+${Versions.MINECRAFT}"
        commitish = Properties.GITHUB_COMMITISH

        allowEmptyFiles = true
    }
}