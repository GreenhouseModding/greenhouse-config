package house.greenhouse.greenhouseconfig.test.client;

import house.greenhouse.greenhouseconfig.test.GreenhouseConfigTest;
import house.greenhouse.greenhouseconfig.test.client.screen.GreenhouseConfigTestScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = GreenhouseConfigTest.MOD_ID, dist = Dist.CLIENT)
public class GreenhouseConfigTestNeoForgeClient {
    public GreenhouseConfigTestNeoForgeClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, (modContainer, screen) -> new GreenhouseConfigTestScreen(screen));
    }

    @EventBusSubscriber(modid = GreenhouseConfigTest.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
    public static class GameEvents {
        @SubscribeEvent
        public static void registerClientCommands(RegisterClientCommandsEvent event) {
            GreenhouseConfigTestClient.registerClientCommands(event.getDispatcher());
        }
    }
}