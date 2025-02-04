package house.greenhouse.greenhouseconfig.impl.client;

import house.greenhouse.greenhouseconfig.impl.GreenhouseConfig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

@Mod(value = GreenhouseConfig.MOD_ID, dist = Dist.CLIENT)
public class GreenhouseConfigNeoForgeClient {

    @EventBusSubscriber(modid = GreenhouseConfig.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    private static class ModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            GreenhouseConfigClient.init();
        }
    }

    @EventBusSubscriber(modid = GreenhouseConfig.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
    private static class GameEvents {
        @SubscribeEvent
        public static void onPlayerJoin(ClientPlayerNetworkEvent.LoggingIn event) {
            GreenhouseConfigClient.onWorldJoin(event.getPlayer().level().registryAccess());
        }
        @SubscribeEvent
        public static void onPlayerLeave(ClientPlayerNetworkEvent.LoggingOut event) {
            GreenhouseConfigClient.onWorldLeave();
        }
    }
}
