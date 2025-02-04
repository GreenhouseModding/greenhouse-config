package house.greenhouse.greenhouseconfig.test.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;

public class GreenhouseConfigTestFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                GreenhouseConfigTestClient.registerClientCommands(dispatcher)
        );
    }
}
