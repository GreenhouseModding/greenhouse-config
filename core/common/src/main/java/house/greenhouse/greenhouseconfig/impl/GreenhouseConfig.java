package house.greenhouse.greenhouseconfig.impl;

import house.greenhouse.greenhouseconfig.platform.GHConfigPlatformHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GreenhouseConfig {
	public static final String MOD_ID = "greenhouseconfig";
	public static final Logger LOG = LoggerFactory.getLogger("Greenhouse Config");
	private static GHConfigPlatformHelper helper;

	public static void init() {
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

	@Deprecated(forRemoval = true, since = "2.1.0+1.21.1")
	public static GHConfigPlatformHelper getPlatform() {
		return getHelper();
	}

	@SuppressWarnings("UnstableApiUsage")
	public static GHConfigPlatformHelper getHelper() {
		if (helper == null) {
			helper = GHConfigPlatformHelper.load();
		}
		return helper;
	}
}
