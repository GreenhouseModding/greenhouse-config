package house.greenhouse.greenhouseconfig.gradle

object Properties {
    const val GROUP = "house.greenhouse"
    const val MOD_AUTHOR = "Greenhouse Team"
    val MOD_CONTRIBUTORS = listOf("MerchantPug", "Kneelawk")
    const val LICENSE = "MPL-2.0"

    const val HOMEPAGE = "https://github.com/GreenhouseModding/greenhouse-config"
    const val GITHUB_REPO = "GreenhouseModding/greenhouse-config"
    const val GITHUB_COMMITISH = "1.21"

    val MODULES = mapOf(
        "core" to ModuleProperties("core", "Greenhouse Config", "greenhouseconfig", "A niche codec based config system.", "1.0.0-alpha.2"),
        "jsonc" to ModuleProperties("jsonc", "Greenhouse Config - JSONC", "greenhouseconfig_jsonc", "JSONC language support for Greenhouse Config.", "1.0.0"),
        "night-config" to ModuleProperties("night-config", "Greenhouse Config - Night Config", "greenhouseconfig_night_config", "Night Config implementation for Greenhouse Config.", "1.0.0"),
        "toml" to ModuleProperties("toml", "Greenhouse Config - TOML", "greenhouseconfig_toml", "TOML language support for Greenhouse Config.", "1.0.0"),
        "yaml" to ModuleProperties("yaml", "Greenhouse Config - YAML", "greenhouseconfig_yaml", "YAML language support for Greenhouse Config.", "1.0.0"),
        "hocon" to ModuleProperties("hocon", "Greenhouse Config - HOCON", "greenhouseconfig_hocon", "HOCON language support for Greenhouse Config.", "1.0.0"),
    )

    val PLATFORMS = setOf(
        "common",
        "fabric",
        "neoforge"
    )

    class ModuleProperties(val moduleName: String, val modName: String, val modId: String, val description: String, val version: String)
}