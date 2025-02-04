import house.greenhouse.greenhouseconfig.gradle.Versions
import house.greenhouse.greenhouseconfig.gradle.props
import org.apache.tools.ant.filters.LineContains
import org.gradle.jvm.tasks.Jar

plugins {
    id("conventions.loader")
    id("net.neoforged.moddev")
    id("me.modmuss50.mod-publish-plugin")
}

dependencies {
    testImplementation(project(":jsonc"))
}

neoForge {
    version = Versions.NEOFORGE
    parchment {
        minecraftVersion = Versions.MINECRAFT
        mappingsVersion = Versions.PARCHMENT
    }
    addModdingDependenciesTo(sourceSets["test"])

    val at = project(":core-common").file("src/main/resources/${props.modId}.cfg")
    if (at.exists())
        setAccessTransformers(at)
    validateAccessTransformers = true

    runs {
        configureEach {
            systemProperty("forge.logging.markers", "REGISTRIES")
            systemProperty("forge.logging.console.level", "debug")
            systemProperty("neoforge.enabledGameTestNamespaces", props.modId)
        }
        create("client") {
            client()
            ideName = "NeoForge Client (:${project.name})"
            gameDirectory.set(file("../../runs/client"))
            sourceSet = sourceSets["test"]
            jvmArguments.set(setOf("-Dmixin.debug.verbose=true", "-Dmixin.debug.export=true"))
        }
        create("server") {
            server()
            ideName = "NeoForge Server (:${project.name})"
            gameDirectory.set(file("../../runs/server"))
            programArgument("--nogui")
            sourceSet = sourceSets["test"]
            jvmArguments.set(setOf("-Dmixin.debug.verbose=true", "-Dmixin.debug.export=true"))
        }
    }

    mods {
        register(props.modId) {
            sourceSet(sourceSets["main"])
        }
        register(props.modId + "_test") {
            sourceSet(sourceSets["test"])
        }
    }
}

tasks {
    named<ProcessResources>("processResources").configure {
        filesMatching("*.mixins.json") {
            filter<LineContains>("negate" to true, "contains" to setOf("refmap"))
        }
    }
}

publishMods {
    github {
        file.set(tasks.named<Jar>("jar").get().archiveFile)
        accessToken = providers.environmentVariable("GITHUB_TOKEN")
        parent(project(":core-common").tasks.named("publishGithub"))
    }
}