package house.greenhouse.greenhouseconfig.impl;

import house.greenhouse.greenhouseconfig.impl.network.QuerySyncGreenhouseConfigPacket;
import house.greenhouse.greenhouseconfig.impl.network.SyncGreenhouseConfigPacket;
import house.greenhouse.greenhouseconfig.platform.GreenhouseConfigNeoForgePlatformHelper;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.network.event.RegisterConfigurationTasksEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@Mod(GreenhouseConfig.MOD_ID)
public class GreenhouseConfigNeoForge {
    private static boolean dedicatedServerContext = false;

    public GreenhouseConfigNeoForge(IEventBus eventBus) {
        GreenhouseConfig.init(new GreenhouseConfigNeoForgePlatformHelper());
    }

    public static boolean isDedicatedServerContext() {
        return dedicatedServerContext;
    }

    @EventBusSubscriber(modid = GreenhouseConfig.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
    public static class ModEvents {
        @SubscribeEvent
        public static void registerConfigurationTasks(RegisterConfigurationTasksEvent event) {
            if (!event.getListener().hasChannel(SyncGreenhouseConfigPacket.TYPE) || event.getListener().getConnection().isMemoryConnection())
                return;
            event.register(new SyncGreenhouseConfigTask(event.getListener()));
        }

        @SubscribeEvent
        public static void registerNetwork(RegisterPayloadHandlersEvent event) {
            event.registrar("1.0.0") // Change this to the version that broke things if something breaks.
                    .optional()
                    .commonToClient(SyncGreenhouseConfigPacket.TYPE, SyncGreenhouseConfigPacket.STREAM_CODEC, (payload, context) -> {
                        if (context.protocol().isConfiguration())
                            payload.handleConfiguration();
                        else
                            payload.handlePlay();
                    })
                    .playToServer(QuerySyncGreenhouseConfigPacket.TYPE, QuerySyncGreenhouseConfigPacket.STREAM_CODEC, (payload, context) -> payload.handle((ServerPlayer) context.player()));
        }
    }

    @EventBusSubscriber(modid = GreenhouseConfig.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
    public static class GameEvents {
        @SubscribeEvent
        public static void onServerStarting(ServerAboutToStartEvent event) {
            if (event.getServer().isDedicatedServer()) {
                dedicatedServerContext = true;
                GreenhouseConfig.onServerStarting();
            }
        }

        @SubscribeEvent
        public static void onServerStarted(ServerStartedEvent event) {
            if (event.getServer().isDedicatedServer())
                GreenhouseConfig.onServerStarted(event.getServer());
        }
    }
}