import house.greenhouse.greenhouseconfig.gradle.Properties
import house.greenhouse.greenhouseconfig.gradle.Versions
import net.fabricmc.loom.task.RemapJarTask
import org.gradle.jvm.tasks.Jar

plugins {
	id("conventions.loader")
	id("fabric-loom")
}

var props = Properties.MODULES["core"]!!

repositories {
	maven("https://maven.terraformersmc.com/") {
		name = "TerraformersMC"
	}
	maven("https://maven.parchmentmc.org") {
		name = "ParchmentMC"
	}
}

dependencies {
	minecraft("com.mojang:minecraft:${Versions.MINECRAFT}")
	mappings(loom.layered {
		officialMojangMappings()
		parchment("org.parchmentmc.data:parchment-1.21.9:${Versions.PARCHMENT}@zip")
	})

	modImplementation("net.fabricmc:fabric-loader:${Versions.FABRIC_LOADER}")
	modImplementation("net.fabricmc.fabric-api:fabric-api:${Versions.FABRIC_API}")

	modCompileOnly("com.terraformersmc:modmenu:${Versions.MOD_MENU}")
	modLocalRuntime("com.terraformersmc:modmenu:${Versions.MOD_MENU}")

	testImplementation(project(":jsonc"))
	testImplementation(project(":toml", configuration = "shaded"))
	testImplementation(project(":yaml", configuration = "shaded"))
	testImplementation(project(":hocon", configuration = "shaded"))
}

// TODO: Split sources test source-sets.
loom {
	val aw = file("src/main/resources/${props.modId}.accesswidener");
	if (aw.exists())
		accessWidenerPath.set(aw)
	mixin {
		defaultRefmapName.set("${props.modId}.refmap.json")
	}
	mods {
		register(props.modId) {
			sourceSet(sourceSets["main"])
		}
		register(props.modId + "_test") {
			sourceSet(sourceSets["test"])
		}
	}
	runs {
		named("client") {
			client()
			configName = "Fabric Client"
			runDir("../../runs/client")
			setSource(sourceSets["test"])
			ideConfigGenerated(true)
			programArgs("--username=Dev")
			vmArgs("-Dmixin.debug.verbose=true", "-Dmixin.debug.export=true")
		}
		named("server") {
			server()
			configName = "Fabric Server"
			runDir("../../runs/server")
			setSource(sourceSets["test"])
			ideConfigGenerated(true)
			vmArgs("-Dmixin.debug.verbose=true", "-Dmixin.debug.export=true")
		}
	}
}

tasks.register<RemapJarTask>("remapCommon") {
	dependsOn(project(":common").tasks.jar)
	inputFile.set(project(":common").tasks.jar.get().archiveFile)

	archiveVersion.set("${props.version}+${Versions.MINECRAFT}-common-intermediary")
}

tasks.register<RemapJarTask>("remapCommonSources") {
	dependsOn(project(":common").tasks.sourcesJar)
	inputFile.set(project(":common").tasks.sourcesJar.get().archiveFile)

	archiveClassifier.set("sources")
	archiveVersion.set("${props.version}+${Versions.MINECRAFT}-common-intermediary")
}

tasks.register<RemapJarTask>("remapCommonJavadoc") {
	dependsOn(project(":common").tasks.javadocJar)
	inputFile.set(project(":common").tasks.javadocJar.get().archiveFile)

	archiveClassifier.set("javadoc")
	archiveVersion.set("${props.version}+${Versions.MINECRAFT}-common-intermediary")
}

tasks.getByName("assemble").dependsOn("remapCommon", "remapCommonSources", "remapCommonJavadoc")

publishing {
	publications {
		create<MavenPublication>("mavenIntermediary") {
			artifactId = props.modId
			version = "${props.version}+${Versions.MINECRAFT}-common-intermediary"
			artifact(tasks["remapCommon"]) {
				builtBy(tasks["remapCommon"])
				classifier = ""
			}
			artifact(tasks["remapCommonSources"]) {
				builtBy(tasks["remapCommonSources"])
				classifier = "sources"
			}
			artifact(tasks["remapCommonJavadoc"]) {
				builtBy(tasks["remapCommonJavadoc"])
				classifier = "javadoc"
			}
		}
	}
}
