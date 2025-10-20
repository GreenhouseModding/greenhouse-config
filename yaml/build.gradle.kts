import house.greenhouse.greenhouseconfig.gradle.Properties
import house.greenhouse.greenhouseconfig.gradle.Versions

plugins {
	id("net.neoforged.moddev")
	id("maven-publish")
	id("com.github.johnrengelman.shadow")
}

var props = Properties.MODULES["yaml"]!!

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
		minecraftVersion = Versions.PARCHMENT_MINECRAFT
		mappingsVersion = Versions.PARCHMENT
	}
}

val shadowInclude = configurations.create("shade")
val jijInclude = configurations.create("jijInclude")
configurations.create("shaded") {
	isCanBeResolved = false
}

dependencies {
	compileOnly(project(":common"))

	implementation("com.electronwill.night-config:yaml:${Versions.NIGHT_CONFIG}")
	shadowInclude("com.electronwill.night-config:yaml:${Versions.NIGHT_CONFIG}")

	api(project(":night-config"))
	jijInclude(project(":night-config")) {
		isTransitive = false
	}
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
		"sources" to Properties.GITHUB_REPO,
		"night_config_version" to Properties.MODULES["night-config"]!!.version
	)

	val processResourcesTasks = listOf("processResources", "processTestResources", "processDatagenResources")

	withType<ProcessResources>().matching { processResourcesTasks.contains(it.name) }.configureEach {
		inputs.properties(expandProps)
		filesMatching(setOf("fabric.mod.json", "META-INF/neoforge.mods.toml", "META-INF/jarjar/metadata.json")) {
			expand(expandProps)
		}
		exclude("\\.cache")
	}

	// GradleUp Shadow jank (we have to double-jar jij dependencies because shadow decompresses all jars)
	val depJar = register<Jar>("depJar") {
		from(jijInclude) {
			into("META-INF/jarjar")
		}
		destinationDirectory.set(layout.buildDirectory.dir("depJar"))
		// gradle jank
		dependsOn(":night-config:shadowJar")
	}

	shadowJar.configure {
		configurations = listOf(shadowInclude)
		archiveClassifier.set("")
		relocate("com.electronwill.nightconfig", "house.greenhouse.greenhouseconfig.nightconfig.shade")
		relocate("org.yaml.snakeyaml", "house.greenhouse.greenhouseconfig.nightconfig.snakeyaml")
		exclude("com/electronwill/nightconfig/core/**")

		from(depJar)

		// more shadow jank
		dependsOn(jar)
	}
}

artifacts.add("shaded", tasks.shadowJar)

publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			from(components["java"])
			artifactId = props.modId
		}
	}
	repositories {
		if (System.getenv("MAVEN_USERNAME") != null && System.getenv("MAVEN_PASSWORD") != null) {
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
		if (System.getenv("PUBLISH_REPO") != null) {
			maven {
				name = "publishRepo"
				url = rootProject.uri(System.getenv("PUBLISH_REPO"))
			}
		}
	}
}
