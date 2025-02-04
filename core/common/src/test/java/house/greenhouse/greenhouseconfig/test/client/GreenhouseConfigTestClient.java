package house.greenhouse.greenhouseconfig.test.client;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import house.greenhouse.greenhouseconfig.api.command.GreenhouseConfigReloadCommandMethods;
import house.greenhouse.greenhouseconfig.test.GreenhouseConfigTest;
import house.greenhouse.greenhouseconfig.test.command.TestCommand;

public class GreenhouseConfigTestClient {
    public static void registerClientCommands(CommandDispatcher<?> dispatcher) {
        LiteralCommandNode<Object> ghTestNode = LiteralArgumentBuilder
                .literal("greenhousetestclient")
                .build();

        LiteralCommandNode<Object> reloadNode = LiteralArgumentBuilder
                .literal("reload")
                .executes(context -> GreenhouseConfigReloadCommandMethods.reloadGreenhouseConfigClient(context, GreenhouseConfigTest.CONFIG))
                .build();

        LiteralCommandNode<Object> colorNode = LiteralArgumentBuilder
                .literal("color")
                .executes(context -> TestCommand.printClientText(context, GreenhouseConfigTest.CONFIG))
                .build();

        ghTestNode.addChild(reloadNode);
        ghTestNode.addChild(colorNode);

        ((CommandDispatcher)dispatcher).getRoot().addChild(ghTestNode);
    }
}