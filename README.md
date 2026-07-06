# ⚠️ Greenhouse Config has PERMANENTLY MOVED to the Greenhouse Forgejo ⚠️

We have been moving our repositories away from GitHub as of lately, and Greenhouse Config is no different.
Please go to the below URL for any issues with the mod.

#### https://git.greenhouse.lgbt/Modding/greenhouse-config

If you still wish to continue to use this repo, here be dragons, shoo!

# Greenhouse Config
Greenhouse Config is a config library for Fabric and NeoForge made for mostly myself (ChrysanthCow), however, any developer is free to utilise it and provide feedback for it.

Please note that Greenhouse Config is currently in alpha, and code may be subject to change.

## Features
- A config system based on Mojang's Codec system but adapted to utilise commented config file formats like jsonc.
- A simple config builder that will handle the heavywork for you.
- Config syncing between servers and clients, without changing the client's individual values.
- LateHolder(Set), a Holder/RegistryEntry or HolderSet/RegistryEntryList that are resolved after datapack registries are populated, but can be loaded before then.
- Backwards compatibility codecs, allowing you to transfer an old schema to a new schema.

## Usage
To learn how to use Greenhouse Config, go to the [Getting Started](https://github.com/GreenhouseTeam/greenhouse-config/wiki/Getting-Started-%E2%80%90-1.x.x) page.

I would suggest checking out the [Wiki](https://github.com/GreenhouseTeam/greenhouse-config/wiki/).

## Maven
Greenhouse Config is on the Greenhouse Team Maven, to get the mod in your environment, please use the bottom as a reference.

```groovy
repositories {
    maven {
        name = "Greenhouse Maven"
        url = "https://maven.greenhouse.lgbt/releases"
    }
}

dependencies {
    // Depend on the Common project, for VanillaGradle and ModDevGradle.
    compileOnly("house.greenhouse:greenhouseconfig:${ghc_version}+${minecraft_version}-common-mojmap")

    // Depend on the Common project, for Loom.
    modCompileOnly("dev.greenhouseteam:greenhouseconfig:${ghc_version}+${minecraft_version}-common-intermediary")

    // Depend on the Fabric project, for Loom.
    modImplementation(include("house.greenhouse:greenhouseconfig:${ghc_version}+${minecraft_version}-fabric"))

    // Depend on the NeoForge project, for ModDevGradle.
    implementation(jarJar("house.greenhouse:greenhouse-config:${ghc_version}+${minecraft_version}-neoforge"))

    // Depend on the NeoForge project, for NeoGradle.
    jarJar(implementation("house.greenhouse:greenhouseconfig:[${ghc_version}+${minecraft_version},)")) {
        version {
            prefer "${ghc_version}+${minecraft_version}-neoforge"
        }
    }
}
```
