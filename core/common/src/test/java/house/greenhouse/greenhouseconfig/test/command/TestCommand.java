package house.greenhouse.greenhouseconfig.test.command;

import com.mojang.brigadier.context.CommandContext;
import house.greenhouse.greenhouseconfig.api.GreenhouseConfigHolder;
import house.greenhouse.greenhouseconfig.impl.GreenhouseConfig;
import house.greenhouse.greenhouseconfig.test.config.SplitConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public class TestCommand {
	public static int printServerText(CommandContext<CommandSourceStack> context, GreenhouseConfigHolder<?> holder) {
		if (!(holder.get() instanceof SplitConfig config))
			return 0;
		context.getSource().sendSuccess(() -> Component.literal(config.common().color().serialize()).withColor(config.common().color().getValue()), false);
		return 1;
	}

	public static int printClientText(CommandContext<?> context, GreenhouseConfigHolder<?> holder) {
		if (!(holder.get() instanceof SplitConfig config))
			return 0;
		GreenhouseConfig.getHelper().sendSuccessClient(context, Component.literal(config.client().color().serialize()).withColor(config.client().color().getValue()));
		return 1;
	}
}
