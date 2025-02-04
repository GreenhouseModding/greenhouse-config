package house.greenhouse.greenhouseconfig.platform;

import com.mojang.brigadier.context.CommandContext;
import house.greenhouse.greenhouseconfig.api.GreenhouseConfigHolder;
import house.greenhouse.greenhouseconfig.api.GreenhouseConfigSide;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.nio.file.Path;

public interface GHConfigIPlatformHelper {

    /**
     * Gets the enum value of the current platform
     *
     * @return The enum value of the current platform.
     */
    Platform getPlatform();

    /**
     * Checks if a mod with the given id is loaded.
     *
     * @param modId The mod to check if it is loaded.
     * @return True if the mod is loaded, false otherwise.
     */
    boolean isModLoaded(String modId);

    /**
     * Check if the game is currently in a development environment.
     *
     * @return True if in a development environment, false otherwise.
     */
    boolean isDevelopmentEnvironment();

    /**
     * Gets the side for the config.
     * This will return SERVER whilst on a dedicated server, otherwise it will return client.
     *
     * @return The side.
     */
    GreenhouseConfigSide getSide();

    /**
     * Gets the config path.
     * @return The config path.
     */
    Path getConfigPath();

    void sendSuccessClient(CommandContext<?> context, Component component);

    void sendFailureClient(CommandContext<?> context, Component component);

    /**
     * Gets the name of the environment type as a string.
     *
     * @return The name of the environment type.
     */
    default String getEnvironmentName() {
        return isDevelopmentEnvironment() ? "development" : "production";
    }

    <T> void syncConfig(GreenhouseConfigHolder<T> holder, MinecraftServer server, ServerPlayer player);

    <T> boolean queryConfig(GreenhouseConfigHolder<T> holder);

    /**
     * Runs the loader specific post load event.
     *
     * @param holder    The config holder.
     * @param config    The value associated with the config holder.
     * @param side      The side of this operation.
     * @param <T>       The config class.
     */
    <T> void postLoadEvent(GreenhouseConfigHolder<T> holder, T config, GreenhouseConfigSide side);

    /**
     * Runs the loader specific post registry population event.
     *
     * @param holder    The config holder.
     * @param config    The value associated with the config holder.
     * @param side      The side of this operation.
     * @param <T>       The config class.
     */
    <T> void postPopulationEvent(GreenhouseConfigHolder<T> holder, T config, GreenhouseConfigSide side);

    /**
     * Runs the loader specific post registry population event.
     *
     * @param holder    The config holder.
     * @param config    The value associated with the config holder.
     * @param side      The side of this operation.
     * @param <T>       The config class.
     */
    <T> void postDepopulationEvent(GreenhouseConfigHolder<T> holder, T config, GreenhouseConfigSide side);
}