package house.greenhouse.greenhouseconfig.impl.client;

import house.greenhouse.greenhouseconfig.impl.GreenhouseConfig;
import house.greenhouse.greenhouseconfig.impl.network.SyncGreenhouseConfigPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.resources.ResourceLocation;

public class GreenhouseConfigFabricClient implements ClientModInitializer {
    public static final ResourceLocation CONFIG_INITIALIZATION_PHASE = GreenhouseConfig.asResource("config_initialization");

    @Override
    public void onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STARTED.addPhaseOrdering(CONFIG_INITIALIZATION_PHASE, Event.DEFAULT_PHASE);
        ClientLifecycleEvents.CLIENT_STARTED.register(CONFIG_INITIALIZATION_PHASE, client -> GreenhouseConfigClient.init());

        ClientConfigurationNetworking.registerGlobalReceiver(SyncGreenhouseConfigPacket.TYPE, (payload, context) -> payload.handleConfiguration());
        ClientPlayNetworking.registerGlobalReceiver(SyncGreenhouseConfigPacket.TYPE, (payload, context) -> payload.handlePlay());

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (client.level != null) {
                GreenhouseConfigClient.onWorldJoin(client.level.registryAccess());
            }
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) ->
                GreenhouseConfigClient.onWorldLeave()
        );
    }
}
