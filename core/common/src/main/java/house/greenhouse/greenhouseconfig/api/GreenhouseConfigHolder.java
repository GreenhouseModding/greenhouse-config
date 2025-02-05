package house.greenhouse.greenhouseconfig.api;

import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Codec;

import house.greenhouse.greenhouseconfig.api.util.LateHolder;
import house.greenhouse.greenhouseconfig.api.util.LateHolderSet;
import house.greenhouse.greenhouseconfig.api.lang.ConfigLang;
import house.greenhouse.greenhouseconfig.api.util.Late;
import house.greenhouse.greenhouseconfig.impl.GreenhouseConfig;
import house.greenhouse.greenhouseconfig.impl.GreenhouseConfigStorage;
import house.greenhouse.greenhouseconfig.impl.GreenhouseConfigHolderRegistry;
import house.greenhouse.greenhouseconfig.impl.GreenhouseConfigHolderImpl;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A holder for configs which has a bunch of helpers methods for accessing and operating on your config.
 * @param <T>   The config class.
 */
public interface GreenhouseConfigHolder<T> {

    /**
     * Constructs a {@link GreenhouseConfigHolder.Builder} with the specified config name.
     * <br>
     * Configs are saved inside the config folder as <b>&lt;config_name&gt;.&lt;file_extension&gt;</b>
     *
     *
     * @param configName The name to create a config under.
     * @param lang The file language to use for this config.
     *             <br>
     *             {@link ConfigLang}s are not built into Greenhouse Config by default, you should instead depend on and include a library with a config lang of choice. Such as the JSONC or TOML lib.
     */
    static <T> Builder<T> builder(String configName, ConfigLang<?> lang) {
        return new Builder<>(configName, lang);
    }

    /**
     * Gets the schema version for this config.
     * @return The schema version.
     */
    int getSchemaVersion();

    /**
     * Gets the config name.
     * @return The config name.
     */
    String getConfigName();

    /**
     * Gets the config file name.
     * It is formatted like config/config_name.file_extension.
     * @return The config file name.
     */
    String getConfigFileName();

    /**
     * Whether this config is network sync-able.
     * @return True if the config is network sync-able, false if not.
     */
    boolean isNetworkSyncable();

    /**
     * Gets the config of this holder.
     * @return The config.
     */
    default T get() {
        return GreenhouseConfigStorage.getConfig((GreenhouseConfigHolderImpl<?, T>) this, false);
    }

    /**
     * Gets the config of this holder without any server-sided changes.
     * Useful for config screens that should obtain the client-sided value.
     * @return The config.
     */
    default T getUnsynced() {
        return GreenhouseConfigStorage.getConfig((GreenhouseConfigHolderImpl<?, T>) this, true);
    }

    /**
     * Saves a config file within the config directory.
     *
     * @param value The config value.
     */
    default void saveConfig(T value) {
        GreenhouseConfigStorage.saveConfig(this, value);
    }

    /**
     * Reloads this config's file.
     * @return The new config, or null if there was an error.
     */
    @Nullable
    default T reloadConfig(Consumer<String> onError) {
        return GreenhouseConfigStorage.reloadConfig((GreenhouseConfigHolderImpl<?, T>) this, onError);
    }

    /**
     * Queries a dedicated server and syncs its config values to the current client.
     * This will return false if the config shouldn't be synced.
     *
     * @return  True if successful, false
     */
    default boolean queryConfig() {
        return GreenhouseConfig.getPlatform().queryConfig(this);
    }

    /**
     * Syncs the config from the server to all clients.
     *
     * @param server The server to sync from.
     */
    default void syncConfig(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers())
            GreenhouseConfig.getPlatform().syncConfig(this, server, player);
    }

    /**
     * Gets the default value for this config holder.
     * @return  The default value of this config holder.
     */
    T getDefaultValue();

    class Builder<T> {
        @NotNull
        private final String configName;
        private final ConfigLang<?> configLang;
        private int schemaVersion = 1;
        private T defaultServerValue;
        private T defaultClientValue;
        private Codec<T> serverCodec;
        private Codec<T> clientCodec;
        private Function<T, StreamCodec<FriendlyByteBuf, T>> networkCodecFunction;
        private BiConsumer<HolderLookup.Provider, T> latePopulationCallback;
        private BiConsumer<HolderLookup.Provider, T> postRegistryPopulationCallback;
        private Consumer<T> lateDepopulationCallback;
        private Consumer<T> postRegistryDepopulationCallback;

        private DataFixer dataFixerServer;
        private DataFixer dataFixerClient;

        protected Builder(@NotNull String configName, ConfigLang<?> lang) {
            this.configName = configName;
            this.configLang = lang;
        }

        /**
         * Sets the config version.
         * The config's version is accessed through the user defined file metadata 'GreenhouseConfigSchemaVersion'.
         *
         * @param version The version of the schema.
         */
        public Builder<T> schemaVersion(int version) {
            this.schemaVersion = Math.max(1, version);
            return this;
        }

        /**
         * Sets a codec for both the dedicated server and the client/integrated server.
         *
         * @param codec The codec to use for both environments.
         * @param defaultValue The default value for this config.
         */
        public Builder<T> common(Codec<T> codec, T defaultValue) {
            server(codec, defaultValue);
            client(codec, defaultValue);
            return this;
        }

        /**
         * The config for use with dedicated servers.
         * <p>
         * This will set the client config is there is no client config value,
         * this is so clients running this mod can still access this config.
         *
         * @param codec The codec to use for serialization.
         * @param defaultValue The default server config value.
         */
        public Builder<T> server(Codec<T> codec, T defaultValue) {
            serverCodec =  codec;
            defaultServerValue = defaultValue;
            return this;
        }

        /**
         * Sets the config for use with clients and integrated servers.
         *
         * @param codec The codec to use for serialization.
         * @param defaultValue The default client config value.
         */
        public Builder<T> client(Codec<T> codec, T defaultValue) {
            clientCodec =  codec;
            defaultClientValue = defaultValue;
            return this;
        }

        /**
         * Sets the config to serialize over the network.
         * Setting specified values from the server on the client.
         * <p>
         * If the client does not have the mod that this config originates from, this will be ignored.
         *
         * @param streamCodec The stream codec to use for serialization.
         */
        public Builder<T> networkSerializable(StreamCodec<FriendlyByteBuf, T> streamCodec) {
            return networkSerializable(clientConfig -> streamCodec);
        }

        /**
         * Sets the config to serialize over the network.
         * Setting specified values from the server on the client.
         * <p>
         * If the client does not have the mod that this config originates from, this will be ignored.
         *
         * @param streamCodecFunction The stream codec to use for serialization whilst passing the current client config.
         */
        public Builder<T> networkSerializable(Function<T, StreamCodec<FriendlyByteBuf, T>> streamCodecFunction) {
            networkCodecFunction = streamCodecFunction;
            return this;
        }

        /**
         * Adds a backwards compatibility codec used for converting from
         * older versions of this config to the current version for
         * both the dedicated server and client/integrated server.
         *
         * @param fixer The DataFixer to use for updating this config.
         * @see com.mojang.datafixers.DataFixerBuilder
         */
        public Builder<T> dataFixer(DataFixer fixer) {
            dataFixerServer(fixer);
            dataFixerClient(fixer);
            return this;
        }

        /**
         * Adds a backwards compatibility codec used for converting from
         * an older version of this config to the current version for
         * the dedicated server.
         * <p>
         * This will additionally set the client backwards compatibility config is there is no client config value,
         * this is so integrated servers running the mod can still access this config.
         *
         * @param fixer The DataFixer to use for updating this config.
         * @see com.mojang.datafixers.DataFixerBuilder
         */
        public Builder<T> dataFixerServer(DataFixer fixer) {
            dataFixerServer = fixer;
            return this;
        }

        /**
         * Adds a backwards compatibility codec used for converting from
         * older versions of this config to the current version for
         * the client/integrated server.
         *
         * @param fixer The DataFixer to use for updating this config.
         * @see com.mojang.datafixers.DataFixerBuilder
         */
        public Builder<T> dataFixerClient(DataFixer fixer) {
            dataFixerClient = fixer;
            return this;
        }

        /**
         * A callback that runs after registries have been populated.
         *
         * @param callback A callback that runs on registry values and the config.
         */
        public Builder<T> postRegistryPopulation(BiConsumer<HolderLookup.Provider, T> callback) {
            postRegistryPopulationCallback = callback;
            return this;
        }

        /**
         * A callback that runs after registries have had their values removed.
         * <br>
         * This typically happens after a client leaves the game.
         *
         * @param callback A callback that runs on the config.
         */
        public Builder<T> postRegistryDepopulation(Consumer<T> callback) {
            postRegistryDepopulationCallback = callback;
            return this;
        }

        /**
         * A shortcut to {@link GreenhouseConfigHolder.Builder#postRegistryPopulation(BiConsumer)} and {@link GreenhouseConfigHolder.Builder#postRegistryDepopulation(Consumer)} that binds/unbinds the late values of this config.
         *
         * @param function A function with the config that should return all late values from your config.
         * @see LateHolder
         * @see LateHolderSet
         */
        public Builder<T> lateValues(Function<T, List<? extends Late>> function, Consumer<String> onException) {
            latePopulationCallback = (lookup, config) -> function.apply(config).forEach(late -> late.bind(lookup, onException));
            lateDepopulationCallback = (config) -> function.apply(config).forEach(Late::unbind);
            return this;
        }

        /**
         * Builds and registers this config.
         * @return A {@link GreenhouseConfigHolder} that holds this config.
         */
        public GreenhouseConfigHolder<T> buildAndRegister() {
            if (configLang == null)
                throw new NullPointerException("Attempted to build config without a language.");

            if (serverCodec == null && clientCodec == null)
                throw new NullPointerException("Attempted to build config for mod " + configName + "without any associated codec.");

            if (defaultServerValue == null && defaultClientValue == null)
                throw new NullPointerException("Attempted to build config without a default value.");

            BiConsumer<HolderLookup.Provider, T> populationCallback = latePopulationCallback != null && postRegistryPopulationCallback != null ? latePopulationCallback.andThen(postRegistryPopulationCallback) : latePopulationCallback != null ? latePopulationCallback : postRegistryPopulationCallback;
            Consumer<T> depopulationCallback = lateDepopulationCallback != null && postRegistryDepopulationCallback != null ? lateDepopulationCallback.andThen(postRegistryDepopulationCallback) : lateDepopulationCallback != null ? lateDepopulationCallback : postRegistryDepopulationCallback;

            GreenhouseConfigHolderImpl<?, T> config = new GreenhouseConfigHolderImpl<>(configName, schemaVersion, configLang, defaultServerValue, defaultClientValue, serverCodec, clientCodec, networkCodecFunction, populationCallback, depopulationCallback, dataFixerServer, dataFixerClient);

            if (serverCodec != null)
                GreenhouseConfigHolderRegistry.registerServerConfig(configName, config);

            if (clientCodec != null)
                GreenhouseConfigHolderRegistry.registerClientConfig(configName, config);

            return config;
        }
    }
}
