package house.greenhouse.greenhouseconfig.impl.client;

import house.greenhouse.greenhouseconfig.impl.network.SyncGreenhouseConfigPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class GreenhouseConfigFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        GreenhouseConfigClient.init();

        ClientConfigurationNetworking.registerGlobalReceiver(SyncGreenhouseConfigPacket.TYPE, (payload, context) -> payload.handleConfiguration());
        ClientPlayNetworking.registerGlobalReceiver(SyncGreenhouseConfigPacket.TYPE, (payload, context) -> payload.handlePlay());

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) ->
                GreenhouseConfigClient.onWorldJoin(client.level.registryAccess())
        );
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) ->
                GreenhouseConfigClient.onWorldLeave()
        );
    }
}
