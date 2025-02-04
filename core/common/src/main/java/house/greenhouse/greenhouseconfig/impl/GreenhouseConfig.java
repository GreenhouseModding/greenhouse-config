package house.greenhouse.greenhouseconfig.impl;

import house.greenhouse.greenhouseconfig.platform.GHConfigIPlatformHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GreenhouseConfig {
    public static final String MOD_ID = "greenhouseconfig";
    public static final Logger LOG = LoggerFactory.getLogger("Greenhouse Config");
    private static GHConfigIPlatformHelper PLATFORM;

    public static void init(GHConfigIPlatformHelper platform) {
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

    public static GHConfigIPlatformHelper getPlatform() {
        return PLATFORM;
    }
}