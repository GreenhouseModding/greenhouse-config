package house.greenhouse.greenhouseconfig.platform;

import com.mojang.brigadier.context.CommandContext;
import house.greenhouse.greenhouseconfig.api.GreenhouseConfigHolder;
import house.greenhouse.greenhouseconfig.api.GreenhouseConfigSide;
import house.greenhouse.greenhouseconfig.api.GreenhouseConfigEvents;
import house.greenhouse.greenhouseconfig.impl.GreenhouseConfigFabric;
import house.greenhouse.greenhouseconfig.impl.network.QuerySyncGreenhouseConfigPacket;
import house.greenhouse.greenhouseconfig.impl.network.SyncGreenhouseConfigPacket;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.nio.file.Path;

public class GreenhouseConfigFabricPlatformHelper implements GHConfigIPlatformHelper {

    @Override
    public Platform getPlatform() {
        return Platform.FABRIC;
    }

    @Override
    public boolean isModLoaded(String modId) {

        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public GreenhouseConfigSide getSide() {
        return GreenhouseConfigFabric.isDedicatedServerContext() ? GreenhouseConfigSide.DEDICATED_SERVER : GreenhouseConfigSide.CLIENT;
    }

    @Override
    public Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public void sendSuccessClient(CommandContext<?> context, Component component) {
        ((FabricClientCommandSource)context.getSource()).sendFeedback(component);
    }

    @Override
    public void sendFailureClient(CommandContext<?> context, Component component) {
        ((FabricClientCommandSource)context.getSource()).sendError(component);
    }

    @Override
    public <T> void syncConfig(GreenhouseConfigHolder<T> holder, MinecraftServer server, ServerPlayer player) {
        if (!holder.isNetworkSyncable() || !ServerPlayNetworking.canSend(player, SyncGreenhouseConfigPacket.TYPE) || server.isSingleplayerOwner(player.getGameProfile()))
            return;
        ServerPlayNetworking.send(player, new SyncGreenhouseConfigPacket(holder.getConfigName(), holder.get()));
    }

    @Override
    public <T> boolean queryConfig(GreenhouseConfigHolder<T> holder) {
        if (!holder.isNetworkSyncable() || !ClientPlayNetworking.canSend(QuerySyncGreenhouseConfigPacket.TYPE) || Minecraft.getInstance().hasSingleplayerServer())
            return false;
        ClientPlayNetworking.send(new QuerySyncGreenhouseConfigPacket(holder));
        return true;
    }

    @Override
    public <T> void postLoadEvent(GreenhouseConfigHolder<T> holder, T config, GreenhouseConfigSide side) {
        GreenhouseConfigEvents.POST_LOAD.invoker().onConfig(holder, config, side);
    }

    @Override
    public <T> void postPopulationEvent(GreenhouseConfigHolder<T> holder, T config, GreenhouseConfigSide side) {
        GreenhouseConfigEvents.POST_POPULATION.invoker().onConfig(holder, config, side);
    }

    @Override
    public <T> void postDepopulationEvent(GreenhouseConfigHolder<T> holder, T config, GreenhouseConfigSide side) {
        GreenhouseConfigEvents.POST_DEPOPULATION.invoker().onConfig(holder, config, side);
    }
}
