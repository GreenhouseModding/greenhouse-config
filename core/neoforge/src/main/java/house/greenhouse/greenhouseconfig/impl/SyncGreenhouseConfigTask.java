package house.greenhouse.greenhouseconfig.impl;

import house.greenhouse.greenhouseconfig.impl.network.SyncGreenhouseConfigPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.neoforged.neoforge.network.configuration.ICustomConfigurationTask;

import java.util.function.Consumer;

public record SyncGreenhouseConfigTask(ServerConfigurationPacketListener listener) implements ICustomConfigurationTask {
    public static final Type TYPE = new Type(SyncGreenhouseConfigPacket.ID);

    @Override
    public void run(Consumer<CustomPacketPayload> sender) {
        GreenhouseConfigStorage.createSyncPackets().forEach(sender);
        listener.finishCurrentTask(TYPE);
    }

    @Override
    public Type type() {
        return TYPE;
    }
}
