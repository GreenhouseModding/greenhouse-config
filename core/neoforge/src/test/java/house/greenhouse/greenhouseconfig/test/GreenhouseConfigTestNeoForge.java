package house.greenhouse.greenhouseconfig.test;

import house.greenhouse.greenhouseconfig.api.GreenhouseConfigEvents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@Mod(GreenhouseConfigTest.MOD_ID)
public class GreenhouseConfigTestNeoForge {
    public GreenhouseConfigTestNeoForge(IEventBus bus) {
        GreenhouseConfigTest.init();
    }

    @EventBusSubscriber(modid = GreenhouseConfigTest.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
    private static class ModEvents {
        @SubscribeEvent
        public static void onPostPopulation(GreenhouseConfigEvents.PostPopulation<?> event) {
            GreenhouseConfigTest.logTestConfigs(event.getHolder(), event.getConfig(), event.getSide());
        }
    }

    @EventBusSubscriber(modid = GreenhouseConfigTest.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
    public static class GameEvents {
        @SubscribeEvent
        public static void registerCommands(RegisterCommandsEvent event) {
            GreenhouseConfigTest.registerServerCommands(event.getDispatcher());
        }
    }
}