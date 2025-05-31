package house.greenhouse.greenhouseconfig.test;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import house.greenhouse.greenhouseconfig.api.GreenhouseConfigHolder;
import house.greenhouse.greenhouseconfig.api.GreenhouseConfigSide;
import house.greenhouse.greenhouseconfig.api.command.GreenhouseConfigReloadCommandMethods;
import house.greenhouse.greenhouseconfig.jsonc.JsonCLang;
import house.greenhouse.greenhouseconfig.test.command.TestCommand;
import house.greenhouse.greenhouseconfig.test.config.SplitConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GreenhouseConfigTest {
	public static final String MOD_ID = "greenhouseconfig_test";
	public static final Logger LOG = LoggerFactory.getLogger("Greenhouse Config Test");

	public static final GreenhouseConfigHolder<SplitConfig> CONFIG = GreenhouseConfigHolder.split(MOD_ID,
					SplitConfig.CLIENT_CODEC,
					SplitConfig.CLIENT_DEFAULT,
					SplitConfig.SERVER_CODEC,
					SplitConfig.SERVER_DEFAULT,
					JsonCLang.INSTANCE)
			.schemaVersion(3)
			.networkSynchronized(SplitConfig::streamCodec)
			.lateValues(SplitConfig::getLateValues, s -> LOG.error("Error handling config/greenhouseconfig_test.jsonc: {}", s))
			.dataFixer(SplitConfig.Fixer.CLIENT, SplitConfig.Fixer.SERVER)
			.build();

	public static void init() {
	}

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
		if (holder == GreenhouseConfigTest.CONFIG && config instanceof SplitConfig(
				SplitConfig.CommonValues common,
				SplitConfig.ClientValues client
		)) {
			GreenhouseConfigTest.LOG.info("Main Config Values...");
			GreenhouseConfigTest.LOG.info("Silly: {}", common.silly());
			GreenhouseConfigTest.LOG.info("Enchantment opinion: {} {}", common.enchantmentOpinion().getSecond().getSerializedName(), common.enchantmentOpinion().getFirst());
			GreenhouseConfigTest.LOG.info(common.redBlocks().toString());
			GreenhouseConfigTest.LOG.info(common.greenBiomes().toString());
			GreenhouseConfigTest.LOG.info("Split Config Values...");
			GreenhouseConfigTest.LOG.info(common.color().serialize());
			if (side == GreenhouseConfigSide.CLIENT) {
				GreenhouseConfigTest.LOG.info(client.color().serialize());
			}
		}
	}

	public static ResourceLocation asResource(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}

}
