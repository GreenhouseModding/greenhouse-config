package house.greenhouse.greenhouseconfig.api.command;

import com.mojang.brigadier.context.CommandContext;
import house.greenhouse.greenhouseconfig.api.GreenhouseConfigHolder;
import house.greenhouse.greenhouseconfig.impl.GreenhouseConfig;
import house.greenhouse.greenhouseconfig.impl.GreenhouseConfigStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;

/**
 * A basic reload command method.
 * Useful for when you wish to add a Greenhouse Config reload command.
 */
public class GreenhouseConfigReloadCommandMethods {
    /**
     * A basic Greenhouse Config reload command method.
     *
     * @param context   The {@link CommandContext} used for this command.
     * @param holder    The config holder.
     * @return          1 if a success, 0 if unsuccessful.
     */
    public static int reloadGreenhouseConfig(CommandContext<CommandSourceStack> context, GreenhouseConfigHolder<?> holder) {
        var config = holder.reloadConfig(s ->
                context.getSource().sendFailure(Component.translatableWithFallback("command.greenhouseconfig.reload.error", "Error whilst reloading config '" + holder.getConfigFileName() + "'.", holder.getConfigFileName()).withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(s)))))
        );
        if (config == null)
            return 0;

        GreenhouseConfigStorage.individualRegistryPopulation(context.getSource().registryAccess(), holder, config);
        if (holder.isNetworkSyncable())
            holder.syncConfig(context.getSource().getServer());

        context.getSource().sendSuccess(() -> Component.translatableWithFallback("command.greenhouseconfig.reload.success", "Successfully reloaded config '" + holder.getConfigFileName() + "'.", holder.getConfigFileName()), true);
        return 1;
    }

    /**
     * A basic Greenhouse Config reload command method for clientside configurations.
     *
     * @param context   The {@link CommandContext} used for this command.
     * @param holder    The config holder.
     * @return          1 if a success, 0 if unsuccessful.
     */
    public static int reloadGreenhouseConfigClient(CommandContext<?> context, GreenhouseConfigHolder<?> holder) {
        var config = holder.reloadConfig(s ->
                GreenhouseConfig.getPlatform().sendFailureClient(context, Component.translatableWithFallback("command.greenhouseconfig.reload.error", "Failed to reload config '" + holder.getConfigFileName() + "'.", holder.getConfigFileName()).withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(s)))))
        );
        if (config == null)
            return 0;

        holder.queryConfig();
        GreenhouseConfigStorage.individualRegistryPopulation(Minecraft.getInstance().level.registryAccess(), holder, config);
        GreenhouseConfig.getPlatform().sendSuccessClient(context, Component.translatableWithFallback("command.greenhouseconfig.reload.success", "Successfully reloading config '" + holder.getConfigFileName() + "'.", holder.getConfigFileName()));
        return 1;
    }
}