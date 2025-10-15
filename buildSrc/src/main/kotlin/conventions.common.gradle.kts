import house.greenhouse.greenhouseconfig.gradle.Properties
import house.greenhouse.greenhouseconfig.gradle.Versions

plugins {
	base
	`java-library`
	idea
	`maven-publish`
}

var props = Properties.MODULES["core"]!!
lateinit var platform: String

Properties.PLATFORMS.forEach { platform ->
	if (project.name == platform) {
		this.platform = platform
	}
}

project.ext["props"] = props

base.archivesName.set(props.modId)
group = Properties.GROUP
version = "${props.version}+${Versions.MINECRAFT}-${project.name}"

java {
	toolchain.languageVersion.set(JavaLanguageVersion.of(Versions.JAVA))
	withSourcesJar()
	withJavadocJar()
}

repositories {
	mavenCentral()
	// https://docs.gradle.org/current/userguide/declaring_repositories.html#declaring_content_exclusively_found_in_one_repository
	exclusiveContent {
		forRepository {
			maven("https://repo.spongepowered.org/repository/maven-public") {
				name = "Sponge"
			}
		}
		filter { includeGroupAndSubgroups("org.spongepowered") }
	}
	maven("https://maven.fabricmc.net/") {
		name = "Fabric"
	}
	exclusiveContent {
		forRepositories(
			maven("https://maven.parchmentmc.org/") {
				name = "ParchmentMC"
			},
			maven("https://maven.neoforged.net/releases") {
				name = "NeoForge"
			}
		)
		filter { includeGroup("org.parchmentmc.data") }
	}
	maven("https://maven.kneelawk.com/releases") {
		name = "Kneelawk"
	}
}

dependencies {
	implementation("org.jetbrains:annotations:24.1.0")
}

tasks {
	named<Jar>("sourcesJar").configure {
		from(rootProject.file("LICENSE")) {
			rename { "${it}_${props.modName}" }
		}
	}
	named<Jar>("jar").configure {
		from(rootProject.file("LICENSE")) {
			rename { "${it}_${props.modName}" }
		}

		manifest {
			attributes["Specification-Title"] = props.modName
			attributes["Specification-Vendor"] = Properties.MOD_AUTHOR
			attributes["Specification-Version"] = archiveVersion
			attributes["Implementation-Title"] = project.name
			attributes["Implementation-Version"] = archiveVersion
			attributes["Implementation-Vendor"] = Properties.MOD_AUTHOR
			attributes["Built-On-Minecraft"] = Versions.MINECRAFT
		}
	}

	val expandProps = mapOf(
		"mod_version" to props.version,
		"group" to project.group, //Else we target the task's group.
		"minecraft_version" to Versions.MINECRAFT,
		"fabric_api_version" to Versions.FABRIC_API,
		"fabric_loader_version" to Versions.FABRIC_LOADER,
		"fabric_minecraft_version_range" to Versions.FABRIC_MINECRAFT_RANGE,
		"fabric_loader_range" to Versions.FABRIC_LOADER_RANGE,
		"mod_name" to props.modName,
		"mod_author" to Properties.MOD_AUTHOR,
		"neoforge_mod_contributors" to Properties.MOD_CONTRIBUTORS.joinToString(),
		"fabric_mod_contributors" to Properties.MOD_CONTRIBUTORS.joinToString(separator = "\",\n\t\t\""),
		"mod_id" to props.modId,
		"mod_license" to Properties.LICENSE,
		"mod_description" to props.description,
		"neoforge_version" to Versions.NEOFORGE,
		"neoforge_minecraft_version_range" to Versions.NEOFORGE_MINECRAFT_RANGE,
		"neoforge_loader_version_range" to Versions.NEOFORGE_LOADER_RANGE,
		"java_version" to Versions.JAVA,
		"homepage" to Properties.HOMEPAGE,
		"sources" to Properties.GITHUB_REPO
	)

	val processResourcesTasks = listOf("processResources", "processTestResources", "processDatagenResources")

	withType<ProcessResources>().matching { processResourcesTasks.contains(it.name) }.configureEach {
		inputs.properties(expandProps)
		filesMatching(setOf("fabric.mod.json", "META-INF/neoforge.mods.toml", "*.mixins.json")) {
			expand(expandProps)
		}
		exclude("\\.cache")
	}
}

publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			from(components["java"])
			artifactId = props.modId
		}
	}
	repositories {
		maven {
			name = "Greenhouse"
			url = uri("https://maven.greenhouse.lgbt/releases")
			credentials {
				username = System.getenv("MAVEN_USERNAME")
				password = System.getenv("MAVEN_PASSWORD")
			}
			authentication {
				create<BasicAuthentication>("basic")
			}
		}
	}
}
