package house.greenhouse.greenhouseconfig.impl;

import house.greenhouse.greenhouseconfig.impl.network.QuerySyncGreenhouseConfigPacket;
import house.greenhouse.greenhouseconfig.impl.network.SyncGreenhouseConfigPacket;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class GreenhouseConfigFabric implements ModInitializer {
	public static void registerPackets() {
		PayloadTypeRegistry.playS2C().register(SyncGreenhouseConfigPacket.TYPE, SyncGreenhouseConfigPacket.STREAM_CODEC);
		PayloadTypeRegistry.configurationS2C().register(SyncGreenhouseConfigPacket.TYPE, SyncGreenhouseConfigPacket.STREAM_CODEC);

		PayloadTypeRegistry.playC2S().register(QuerySyncGreenhouseConfigPacket.TYPE, QuerySyncGreenhouseConfigPacket.STREAM_CODEC);
		ServerPlayNetworking.registerGlobalReceiver(QuerySyncGreenhouseConfigPacket.TYPE, (payload, context) -> payload.handle(context.player()));
	}

	public static void registerEvents() {
		ServerConfigurationConnectionEvents.BEFORE_CONFIGURE.register((handler, server) -> {
			if (!ServerConfigurationNetworking.canSend(handler, SyncGreenhouseConfigPacket.TYPE) || server.isSingleplayerOwner(handler.getOwner()))
				return;
			GreenhouseConfigStorage.createSyncPackets().forEach(packet -> ServerConfigurationNetworking.send(handler, packet));
		});
		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			if (server.isDedicatedServer()) {
				GreenhouseConfig.onServerStarting();
			}
		});
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			if (server.isDedicatedServer())
				GreenhouseConfig.onServerStarted(server);
		});
	}

	@Override
	public void onInitialize() {
		registerPackets();
		registerEvents();
	}
}
