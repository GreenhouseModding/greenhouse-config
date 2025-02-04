package house.greenhouse.greenhouseconfig.platform;

import com.mojang.brigadier.context.CommandContext;
import house.greenhouse.greenhouseconfig.api.GreenhouseConfigHolder;
import house.greenhouse.greenhouseconfig.impl.GreenhouseConfigNeoForge;
import house.greenhouse.greenhouseconfig.api.GreenhouseConfigSide;
import house.greenhouse.greenhouseconfig.api.GreenhouseConfigEvents;
import house.greenhouse.greenhouseconfig.impl.network.QuerySyncGreenhouseConfigPacket;
import house.greenhouse.greenhouseconfig.impl.network.SyncGreenhouseConfigPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.ClientCommandSourceStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.nio.file.Path;

public class GreenhouseConfigNeoForgePlatformHelper implements GHConfigIPlatformHelper {
    @Override
    public Platform getPlatform() {
        return Platform.NEOFORGE;
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.isProduction();
    }

    @Override
    public GreenhouseConfigSide getSide() {
        return GreenhouseConfigNeoForge.isDedicatedServerContext() ? GreenhouseConfigSide.DEDICATED_SERVER : GreenhouseConfigSide.CLIENT;
    }

    @Override
    public Path getConfigPath() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public void sendSuccessClient(CommandContext<?> context, Component component) {
        ((ClientCommandSourceStack)context.getSource()).sendSuccess(() -> component, false);
    }

    @Override
    public void sendFailureClient(CommandContext<?> context, Component component) {
        ((ClientCommandSourceStack)context.getSource()).sendFailure(component);
    }

    @Override
    public <T> void syncConfig(GreenhouseConfigHolder<T> holder, MinecraftServer server, ServerPlayer player) {
        if (!holder.isNetworkSyncable() || !player.connection.hasChannel(SyncGreenhouseConfigPacket.TYPE) || server.isSingleplayerOwner(player.getGameProfile()))
            return;
        PacketDistributor.sendToPlayer(player, new SyncGreenhouseConfigPacket(holder.getConfigName(), holder.get()));
    }

    @Override
    public <T> boolean queryConfig(GreenhouseConfigHolder<T> holder) {
        if (!holder.isNetworkSyncable() || !Minecraft.getInstance().getConnection().hasChannel(SyncGreenhouseConfigPacket.TYPE) || Minecraft.getInstance().hasSingleplayerServer())
            return false;
        PacketDistributor.sendToServer(new QuerySyncGreenhouseConfigPacket(holder));
        return true;
    }

    @Override
    public <T> void postLoadEvent(GreenhouseConfigHolder<T> holder, T config, GreenhouseConfigSide side) {
        GreenhouseConfigEvents.PostLoad.post(holder, config, side);
    }

    @Override
    public <T> void postPopulationEvent(GreenhouseConfigHolder<T> holder, T config, GreenhouseConfigSide side) {
        GreenhouseConfigEvents.PostPopulation.post(holder, config, side);
    }

    @Override
    public <T> void postDepopulationEvent(GreenhouseConfigHolder<T> holder, T config, GreenhouseConfigSide side) {
        GreenhouseConfigEvents.PostDepopulation.post(holder, config, side);
    }
}