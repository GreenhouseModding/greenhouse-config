import house.greenhouse.greenhouseconfig.gradle.Properties
import house.greenhouse.greenhouseconfig.gradle.Versions

plugins {
	id("net.neoforged.moddev")
	id("me.modmuss50.mod-publish-plugin")
	id("maven-publish")
}

var props = Properties.MODULES["jsonc"]!!

base.archivesName.set(props.modId)
group = Properties.GROUP
version = props.version

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
}

dependencies {
	compileOnly(project(":common"))
}

tasks {
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
		"mod_name" to props.modName,
		"mod_author" to Properties.MOD_AUTHOR,
		"neoforge_mod_contributors" to Properties.MOD_CONTRIBUTORS.joinToString(),
		"fabric_mod_contributors" to Properties.MOD_CONTRIBUTORS.joinToString(separator = "\",\n\t\t\""),
		"mod_id" to props.modId,
		"mod_license" to Properties.LICENSE,
		"mod_description" to props.description,
		"neoforge_version" to Versions.NEOFORGE,
		"java_version" to Versions.JAVA,
		"homepage" to Properties.HOMEPAGE,
		"sources" to Properties.GITHUB_REPO
	)

	val processResourcesTasks = listOf("processResources", "processTestResources", "processDatagenResources")

	withType<ProcessResources>().matching { processResourcesTasks.contains(it.name) }.configureEach {
		inputs.properties(expandProps)
		filesMatching(setOf("fabric.mod.json", "META-INF/neoforge.mods.toml")) {
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
