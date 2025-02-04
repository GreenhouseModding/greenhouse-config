package house.greenhouse.greenhouseconfig.test.command;

import com.mojang.brigadier.context.CommandContext;
import house.greenhouse.greenhouseconfig.api.GreenhouseConfigHolder;
import house.greenhouse.greenhouseconfig.impl.GreenhouseConfig;
import house.greenhouse.greenhouseconfig.test.config.TestConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public class TestCommand {
    public static int printServerText(CommandContext<CommandSourceStack> context, GreenhouseConfigHolder<?> holder) {
        if (!(holder.get() instanceof TestConfig config))
            return 0;
        context.getSource().sendSuccess(() -> Component.literal(config.color().serialize()).withColor(config.color().getValue()), false);
        return 1;
    }

    public static int printClientText(CommandContext<?> context, GreenhouseConfigHolder<?> holder) {
        if (!(holder.get() instanceof TestConfig config))
            return 0;
        GreenhouseConfig.getPlatform().sendSuccessClient(context, Component.literal(config.clientValues().color().serialize()).withColor(config.clientValues().color().getValue()));
        return 1;
    }
}
