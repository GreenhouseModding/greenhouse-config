package house.greenhouse.greenhouseconfig.impl.network;

import house.greenhouse.greenhouseconfig.api.GreenhouseConfigHolder;
import house.greenhouse.greenhouseconfig.impl.GreenhouseConfig;
import house.greenhouse.greenhouseconfig.impl.GreenhouseConfigHolderRegistry;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public record QuerySyncGreenhouseConfigPacket(GreenhouseConfigHolder<?> holder) implements CustomPacketPayload {
    public static final ResourceLocation ID = GreenhouseConfig.asResource("query_sync_config");
    public static final Type<QuerySyncGreenhouseConfigPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<ByteBuf, QuerySyncGreenhouseConfigPacket> STREAM_CODEC =
            ByteBufCodecs.STRING_UTF8.map(key -> new QuerySyncGreenhouseConfigPacket(GreenhouseConfigHolderRegistry.CLIENT_CONFIG_HOLDERS.get(key)), config -> config.holder().getConfigName());

    public void handle(ServerPlayer player) {
        player.getServer().execute(() ->
                GreenhouseConfig.getPlatform().syncConfig(holder, player.server, player)
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
