package house.greenhouse.greenhouseconfig.impl;

import house.greenhouse.greenhouseconfig.platform.GHConfigPlatformHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GreenhouseConfig {
	public static final String MOD_ID = "greenhouseconfig";
	public static final Logger LOG = LoggerFactory.getLogger("Greenhouse Config");
	private static GHConfigPlatformHelper PLATFORM;

	public static void init(GHConfigPlatformHelper platform) {
		if (PLATFORM != null)
			return;
		PLATFORM = platform;
	}

	public static ResourceLocation asResource(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}

	public static void onServerStarting() {
		GreenhouseConfigStorage.generateServerConfigs();
	}

	public static void onServerStarted(MinecraftServer server) {
		GreenhouseConfigStorage.onRegistryPopulation(server.registryAccess());
	}

	public static GHConfigPlatformHelper getPlatform() {
		return PLATFORM;
	}
}
