import house.greenhouse.greenhouseconfig.gradle.Properties

plugins {
    id("conventions.common")
}

lateinit var props: Properties.ModuleProperties;

Properties.MODULES.forEach { (name, metadata) ->
    Properties.PLATFORMS.forEach { platform ->
        if (project.name == "${name}-${platform}")
            props = metadata
    }
}

fun getCommonProjectName() : String {
    return props.moduleName + "-common"
}

configurations {
    register("commonJava") {
        isCanBeResolved = true
    }
    register("commonTestJava") {
        isCanBeResolved = true
    }
    register("commonResources") {
        isCanBeResolved = true
    }
    register("commonTestResources") {
        isCanBeResolved = true
    }
}

dependencies {
    compileOnly(project(":${getCommonProjectName()}"))
    testCompileOnly(project(":${getCommonProjectName()}", "commonJava"))
    testCompileOnly(project(":${getCommonProjectName()}", "commonTestJava"))
    "commonJava"(project(":${getCommonProjectName()}", "commonJava"))
    "commonTestJava"(project(":${getCommonProjectName()}", "commonTestJava"))
    "commonResources"(project(":${getCommonProjectName()}", "commonResources"))
    "commonTestResources"(project(":${getCommonProjectName()}", "commonTestResources"))
}

tasks {
    named<JavaCompile>("compileJava").configure {
        dependsOn(configurations.getByName("commonJava"))
        source(configurations.getByName("commonJava"))
    }
    named<JavaCompile>("compileTestJava").configure {
        dependsOn(configurations.getByName("commonTestJava"))
        source(configurations.getByName("commonTestJava"))
    }
    named<ProcessResources>("processResources").configure {
        dependsOn(configurations.getByName("commonResources"))
        from(configurations.getByName("commonResources"))
        from(configurations.getByName("commonResources"))
    }
    named<ProcessResources>("processTestResources").configure {
        dependsOn(configurations.getByName("commonTestResources"))
        from(configurations.getByName("commonTestResources"))
        from(configurations.getByName("commonTestResources"))
    }
    named<Javadoc>("javadoc").configure {
        dependsOn(configurations.getByName("commonJava"))
        source(configurations.getByName("commonJava"))
    }
    named<Jar>("sourcesJar").configure {
        dependsOn(configurations.getByName("commonJava"))
        from(configurations.getByName("commonJava"))
        dependsOn(configurations.getByName("commonResources"))
        from(configurations.getByName("commonResources"))
    }
}