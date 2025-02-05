package house.greenhouse.greenhouseconfig.test;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import house.greenhouse.greenhouseconfig.api.GreenhouseConfigHolder;
import house.greenhouse.greenhouseconfig.api.GreenhouseConfigSide;
import house.greenhouse.greenhouseconfig.api.command.GreenhouseConfigReloadCommandMethods;
import house.greenhouse.greenhouseconfig.jsonc.JsonCLang;
import house.greenhouse.greenhouseconfig.test.command.TestCommand;
import house.greenhouse.greenhouseconfig.test.config.TestConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GreenhouseConfigTest {
    public static final String MOD_ID = "greenhouseconfig_test";
    public static final Logger LOG = LoggerFactory.getLogger("Greenhouse Config Test");

    public static final GreenhouseConfigHolder<TestConfig> CONFIG = GreenhouseConfigHolder.<TestConfig>builder(MOD_ID, JsonCLang.INSTANCE)
            .schemaVersion(3)
            .common(TestConfig.CODEC, TestConfig.DEFAULT)
            .networkSerializable(TestConfig::streamCodec)
            .lateValues(TestConfig::getLateValues, s -> LOG.error("Error handling config/greenhouseconfig_test.jsonc: {}", s))
            .dataFixer(TestConfig.Fixer.INSTANCE)
            .buildAndRegister();

    public static void init() {}

    public static void registerServerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> ghTestNode = Commands
                .literal("greenhousetest")
                .build();

        LiteralCommandNode<CommandSourceStack> reloadNode = Commands
                .literal("reload")
                .executes(context -> GreenhouseConfigReloadCommandMethods.reloadGreenhouseConfig(context, CONFIG))
                .build();

        LiteralCommandNode<CommandSourceStack> colorNode = Commands
                .literal("color")
                .executes(context -> TestCommand.printServerText(context, CONFIG))
                .build();

        ghTestNode.addChild(reloadNode);
        ghTestNode.addChild(colorNode);

        dispatcher.getRoot().addChild(ghTestNode);
    }

    public static void logTestConfigs(GreenhouseConfigHolder<?> holder, Object config, GreenhouseConfigSide side) {
        if (holder == GreenhouseConfigTest.CONFIG && config instanceof TestConfig testConfig) {
            GreenhouseConfigTest.LOG.info("Main Config Values...");
            GreenhouseConfigTest.LOG.info("Silly: {}", testConfig.silly());
            GreenhouseConfigTest.LOG.info("Enchantment opinion: {} {}", testConfig.enchantmentOpinion().getSecond().getSerializedName(), testConfig.enchantmentOpinion().getFirst());
            GreenhouseConfigTest.LOG.info(testConfig.redBlocks().toString());
            GreenhouseConfigTest.LOG.info(testConfig.greenBiomes().toString());
            GreenhouseConfigTest.LOG.info("Split Config Values...");
            GreenhouseConfigTest.LOG.info(testConfig.color().serialize());
            if (side == GreenhouseConfigSide.CLIENT)
                GreenhouseConfigTest.LOG.info(testConfig.clientValues().color().serialize());
        }
    }

    public static ResourceLocation asResource(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

}
