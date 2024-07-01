package dev.greenhouseteam.greenhouseconfig.test;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.greenhouseteam.greenhouseconfig.api.GreenhouseConfigHolder;
import dev.greenhouseteam.greenhouseconfig.api.command.GreenhouseConfigReloadCommandMethods;
import dev.greenhouseteam.greenhouseconfig.api.util.LateHolderSet;
import dev.greenhouseteam.greenhouseconfig.test.config.SplitConfig;
import dev.greenhouseteam.greenhouseconfig.test.config.TestConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GreenhouseConfigTest {
    public static final String MOD_ID = "greenhouseconfig_test";
    public static final Logger LOG = LoggerFactory.getLogger("Greenhouse Config Test");

    public static final GreenhouseConfigHolder<TestConfig> CONFIG = new GreenhouseConfigHolder.Builder<TestConfig>(MOD_ID + "/main")
            /*
            .configVersion(1)
            .commonCodec(TestConfig.CompatCodecs.V1, TestConfig.DEFAULT)
            */
            .schemaVersion(2)
            .common(TestConfig.CODEC, TestConfig.DEFAULT)
            .networkSerializable(TestConfig.STREAM_CODEC)
            .postRegistryPopulation((provider, testConfig) -> {
                LateHolderSet.bind(
                        BuiltInRegistries.BLOCK.asLookup(),
                        testConfig.redBlocks(),
                        s -> LOG.error("Error while parsing \"red_blocks\" in config/greenhouseconfig_test.jsonc: {}", s)
                );
                LateHolderSet.bind(
                        provider.lookupOrThrow(Registries.BIOME),
                        testConfig.greenBiomes(),
                        s -> LOG.error("Error while parsing \"green_biomes\" in config/greenhouseconfig_test.jsonc: {}", s)
                );
            })
            .backwardsCompatCommon(1, TestConfig.CompatCodecs.V1)
            .buildAndRegister();

    public static final GreenhouseConfigHolder<SplitConfig> SPLIT = new GreenhouseConfigHolder.Builder<SplitConfig>(MOD_ID + "/split")
            .schemaVersion(1)
            .server(SplitConfig.SERVER_CODEC, SplitConfig.DEFAULT)
            .client(SplitConfig.CLIENT_CODEC, SplitConfig.DEFAULT)
            .networkSerializable(SplitConfig::streamCodec)
            .buildAndRegister();

    public static final GreenhouseConfigHolder<SplitConfig> SERVER = new GreenhouseConfigHolder.Builder<SplitConfig>(MOD_ID + "/server")
            .schemaVersion(1)
            .server(SplitConfig.SERVER_CODEC, SplitConfig.DEFAULT)
            .buildAndRegister();

    public static final GreenhouseConfigHolder<SplitConfig> CLIENT = new GreenhouseConfigHolder.Builder<SplitConfig>(MOD_ID + "/client")
            .schemaVersion(1)
            .client(SplitConfig.CLIENT_CODEC, SplitConfig.DEFAULT)
            .buildAndRegister();

    public static void init() {
    }

    public static void registerServerReloadCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> ghTestNode = Commands
                .literal("greenhouse_test")
                .build();

        LiteralCommandNode<CommandSourceStack> reloadNode = Commands
                .literal("reload")
                .build();

        LiteralCommandNode<CommandSourceStack> reloadMainNode = Commands
                .literal("main")
                .executes(context -> GreenhouseConfigReloadCommandMethods.reloadGreenhouseConfig(context, CONFIG))
                .build();

        LiteralCommandNode<CommandSourceStack> reloadSplitServerNode = Commands
                .literal("split")
                .executes(context -> GreenhouseConfigReloadCommandMethods.reloadGreenhouseConfig(context, SPLIT))
                .build();

        LiteralCommandNode<CommandSourceStack> reloadServerNode = Commands
                .literal("server")
                .executes(context -> GreenhouseConfigReloadCommandMethods.reloadGreenhouseConfig(context, SPLIT))
                .build();

        reloadNode.addChild(reloadMainNode);
        reloadNode.addChild(reloadSplitServerNode);
        reloadNode.addChild(reloadServerNode);

        ghTestNode.addChild(reloadNode);

        dispatcher.getRoot().addChild(ghTestNode);
    }

    public static ResourceLocation asResource(String path) {
        return ResourceLocation.tryBuild(MOD_ID, path);
    }

}
