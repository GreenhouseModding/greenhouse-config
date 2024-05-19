package dev.greenhouseteam.greenhouseconfig.test;

import dev.greenhouseteam.greenhouseconfig.api.GreenhouseConfigHolder;
import dev.greenhouseteam.greenhouseconfig.api.util.LateHolderSet;
import dev.greenhouseteam.greenhouseconfig.test.config.TestConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;

public class GreenhouseConfigTest {
    private static final String MOD_ID = "greenhouseconfig_test";

    private static final GreenhouseConfigHolder<TestConfig> CONFIG = new GreenhouseConfigHolder.Builder<TestConfig>(MOD_ID)
            .configVersion(1)
            .commonCodec(TestConfig.TEST_CONFIG_CODEC, TestConfig.DEFAULT)
            .postRegistryPopulation((provider, testConfig) -> {
                LateHolderSet.bind(provider.lookupOrThrow(Registries.BIOME), testConfig.greenBiomes());
            })
            .buildAndRegister();

    public static void init() {

    }

    public static ResourceLocation asResource(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

}
