package house.greenhouse.greenhouseconfig.api;

import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Codec;
import house.greenhouse.greenhouseconfig.api.lang.ConfigLang;
import house.greenhouse.greenhouseconfig.api.util.Late;
import house.greenhouse.greenhouseconfig.api.util.LateHolder;
import house.greenhouse.greenhouseconfig.api.util.LateHolderSet;
import house.greenhouse.greenhouseconfig.impl.GreenhouseConfig;
import house.greenhouse.greenhouseconfig.impl.GreenhouseConfigHolderImpl;
import house.greenhouse.greenhouseconfig.impl.GreenhouseConfigHolderRegistry;
import house.greenhouse.greenhouseconfig.impl.GreenhouseConfigStorage;
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
 * <br>
 * Configs are saved inside the config folder as <b>&lt;config_name&gt;.&lt;file_extension&gt;</b>
 *
 * @param <T> The config class.
 */
public interface GreenhouseConfigHolder<T> {
	/**
	 * Creates a config builder that is set up to be shared between the client and the server.
	 *
	 * @param configName   The name of the config to create.
	 * @param codec        The codec used to serialize/deserialize the config.
	 * @param defaultValue An initial/default value for this config.
	 * @param lang         The file language to use for this config.
	 *                     <br>
	 *                     {@link ConfigLang}s are not built into Greenhouse Config by default, you should instead depend on and include a library with a config lang of choice. Such as the JSONC or TOML lib.
	 * @param <T>          The config object.
	 * @return A config builder.
	 */
	static <T> Builder<T> common(String configName,
								 Codec<T> codec,
								 T defaultValue,
								 ConfigLang<?> lang) {
		return new Builder<>(configName, codec, codec, defaultValue, defaultValue, lang);
	}

	/**
	 * Creates a config builder that is set up to have separate client and the server values.
	 *
	 * @param configName    The name of the config to create.
	 * @param clientCodec   The client-sided codec used to serialize/deserialize the config.
	 * @param clientDefault An initial/default value for this config.
	 * @param lang          The file language to use for this config.
	 *                      <br>
	 *                      {@link ConfigLang}s are not built into Greenhouse Config by default, you should instead depend on and include a library with a config lang of choice. Such as the JSONC or TOML lib.
	 * @param <T>           The config object.
	 * @return A config builder.
	 */
	static <T> Builder<T> split(String configName,
								Codec<T> clientCodec,
								T clientDefault,
								Codec<T> serverCodec,
								T serverDefault,
								ConfigLang<?> lang) {
		return new Builder<>(configName, clientCodec, serverCodec, clientDefault, serverDefault, lang);
	}

	/**
	 * Creates a config builder that is set up to only be present on the client .
	 *
	 * @param configName   The name of the config to create.
	 * @param codec        The codec used to serialize/deserialize the config.
	 * @param defaultValue An initial/default value for this config.
	 * @param lang         The file language to use for this config.
	 *                     <br>
	 *                     {@link ConfigLang}s are not built into Greenhouse Config by default, you should instead depend on and include a library with a config lang of choice. Such as the JSONC or TOML lib.
	 * @param <T>          The config object.
	 * @return A config builder.
	 */
	static <T> Builder<T> client(String configName,
								 Codec<T> codec,
								 T defaultValue,
								 ConfigLang<?> lang) {
		return new Builder<>(configName, codec, null, defaultValue, null, lang);
	}

	/**
	 * Creates a config builder that is set up to only be present on the server.
	 *
	 * @param configName   The name of the config to create.
	 * @param codec        The codec used to serialize/deserialize the config.
	 * @param defaultValue An initial/default value for this config.
	 * @param lang         The file language to use for this config.
	 *                     <br>
	 *                     {@link ConfigLang}s are not built into Greenhouse Config by default, you should instead depend on and include a library with a config lang of choice. Such as the JSONC or TOML lib.
	 * @param <T>          The config object.
	 * @return A config builder.
	 */
	static <T> Builder<T> dedicatedServer(String configName,
										  Codec<T> codec,
										  T defaultValue,
										  ConfigLang<?> lang) {
		return new Builder<>(configName, null, codec, null, defaultValue, lang);
	}

	/**
	 * Constructs a {@link GreenhouseConfigHolder.Builder} with the specified config name.
	 * <br>
	 * Configs are saved inside the config folder as <b>&lt;config_name&gt;.&lt;file_extension&gt;</b>
	 *
	 * @param configName The name to create a config under.
	 * @param lang       The file language to use for this config.
	 *                   <br>
	 *                   {@link ConfigLang}s are not built into Greenhouse Config by default, you should instead depend on and include a library with a config lang of choice. Such as the JSONC or TOML lib.
	 */
	@Deprecated(forRemoval = true, since = "2.0.0")
	static <T> Builder<T> builder(String configName, ConfigLang<?> lang) {
		return new Builder<>(configName, lang);
	}

	/**
	 * Gets the schema version for this config.
	 *
	 * @return The schema version.
	 */
	int getSchemaVersion();

	/**
	 * Gets the config name.
	 *
	 * @return The config name.
	 */
	String getConfigName();

	/**
	 * Gets the config file name.
	 * It is formatted like config/config_name.file_extension.
	 *
	 * @return The config file name.
	 */
	String getConfigFileName();

	/**
	 * Whether this config should sync.
	 *
	 * @return True if the config should sync from server to client, false if not.
	 */
	boolean shouldSync();

	/**
	 * Whether this config is network sync-able.
	 *
	 * @return True if the config is network sync-able, false if not.
	 * @see    GreenhouseConfigHolder#shouldSync()
	 */
	@Deprecated(forRemoval = true, since = "2.0.0")
	default boolean isNetworkSyncable() {
		return shouldSync();
	}

	/**
	 * Gets the config of this holder.
	 *
	 * @return The config.
	 */
	@Nullable
	default T get() {
		return GreenhouseConfigStorage.getConfig((GreenhouseConfigHolderImpl<?, T>) this, false, false);
	}

	/**
	 * Gets the config of this holder without any server-sided changes.
	 * Useful for config screens that should obtain the client-sided value.
	 *
	 * @return The config.
	 * @throws NullPointerException If the unsynced config is not present.
	 */
	default T getOrThrow() {
		return GreenhouseConfigStorage.getConfig((GreenhouseConfigHolderImpl<?, T>) this, false, true);
	}

	/**
	 * Gets the config of this holder without any server-sided changes.
	 * Useful for config screens that should obtain the client-sided value.
	 *
	 * @return The unsynced config.
	 */
	@Nullable
	default T getUnsynced() {
		return GreenhouseConfigStorage.getConfig((GreenhouseConfigHolderImpl<?, T>) this, true, false);
	}

	/**
	 * Gets the config of this holder without any server-sided changes.
	 * Useful for config screens that should obtain the client-sided value.
	 *
	 * @return The unsynced config.
	 * @throws NullPointerException If the unsynced config is not present.
	 */
	default T getUnsyncedOrThrow() {
		return GreenhouseConfigStorage.getConfig((GreenhouseConfigHolderImpl<?, T>) this, true, true);
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
	 *
	 * @return The new config, or null if there was an error.
	 */
	@Nullable
	default T reloadConfig(Consumer<String> onError) {
		return GreenhouseConfigStorage.reloadConfig((GreenhouseConfigHolderImpl<?, T>) this, onError);
	}

	/**
	 * Queries a dedicated server and syncs its config values to the current client.
	 * This will return false if the config shouldn't be synced.
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
	 *
	 * @return The default value of this config holder.
	 */
	T getDefaultValue();

	class Builder<T> {
		@NotNull
		private final String configName;
		@NotNull
		private final ConfigLang<?> configLang;

		private int schemaVersion = 1;

		@Nullable
		private Codec<T> serverCodec;
		@Nullable
		private Codec<T> clientCodec;

		@Nullable
		private T defaultServerValue;
		@Nullable
		private T defaultClientValue;
		@Nullable
		private Function<T, StreamCodec<FriendlyByteBuf, T>> networkFunction;

		@Nullable
		private BiConsumer<HolderLookup.Provider, T> latePopulationCallback;
		@Nullable
		private Consumer<T> lateDepopulationCallback;

		@Nullable
		private BiConsumer<HolderLookup.Provider, T> postRegistryPopulationCallback;
		@Nullable
		private Consumer<T> postRegistryDepopulationCallback;

		@Nullable
		private DataFixer clientFixer;
		@Nullable
		private DataFixer serverFixer;

		protected Builder(@NotNull String configName,
						  @Nullable Codec<T> clientCodec,
						  @Nullable Codec<T> serverCodec,
						  @Nullable T defaultClientValue,
						  @Nullable T defaultServerValue,
						  @NotNull ConfigLang<?> lang) {
			this.configName = configName;
			this.clientCodec = clientCodec;
			this.serverCodec = serverCodec;
			this.defaultClientValue = defaultClientValue;
			this.defaultServerValue = defaultServerValue;
			this.configLang = lang;
		}

		@Deprecated(forRemoval = true, since = "2.0.0")
		protected Builder(@NotNull String configName,
						  @NotNull ConfigLang<?> lang) {
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
		 * @param codec        The codec to use for both environments.
		 * @param defaultValue The default value for this config.
		 * @see    GreenhouseConfigHolder#common(String, Codec, Object, ConfigLang)
		 */
		@Deprecated(forRemoval = true, since = "2.0.0")
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
		 * @param codec        The codec to use for serialization.
		 * @param defaultValue The default server config value.
		 */
		@Deprecated(forRemoval = true, since = "2.0.0")
		public Builder<T> server(Codec<T> codec, T defaultValue) {
			serverCodec = codec;
			defaultServerValue = defaultValue;
			return this;
		}

		/**
		 * Sets the config for use with clients and integrated servers.
		 *
		 * @param codec        The codec to use for serialization.
		 * @param defaultValue The default client config value.
		 */
		@Deprecated(forRemoval = true, since = "2.0.0")
		public Builder<T> client(Codec<T> codec, T defaultValue) {
			clientCodec = codec;
			defaultClientValue = defaultValue;
			return this;
		}

		/**
		 * @see GreenhouseConfigHolder.Builder#networkSynchronized(StreamCodec)
		 */
		@Deprecated(forRemoval = true, since = "2.0.0")
		public Builder<T> networkSerializable(StreamCodec<FriendlyByteBuf, T> streamCodec) {
			return networkSynchronized(streamCodec);
		}

		/**
		 * @see GreenhouseConfigHolder.Builder#networkSynchronized(Function)
		 */
		@Deprecated(forRemoval = true, since = "2.0.0")
		public Builder<T> networkSerializable(Function<T, StreamCodec<FriendlyByteBuf, T>> streamCodecFunction) {
			return networkSynchronized(streamCodecFunction);
		}

		/**
		 * Sets the config to serialize over the network.
		 * Setting specified values from the server on the client.
		 * <p>
		 * If the client does not have the mod that this config originates from, this will be ignored.
		 *
		 * @param streamCodec The stream codec to use for serialization.
		 */
		public Builder<T> networkSynchronized(StreamCodec<FriendlyByteBuf, T> streamCodec) {
			return networkSynchronized(clientConfig -> streamCodec);
		}

		/**
		 * Sets the config to serialize over the network.
		 * Setting specified values from the server on the client.
		 * <p>
		 * If the client does not have the mod that this config originates from, this will be ignored.
		 *
		 * @param streamCodecFunction The stream codec to use for serialization whilst passing the current client config.
		 */
		public Builder<T> networkSynchronized(Function<T, StreamCodec<FriendlyByteBuf, T>> streamCodecFunction) {
			this.networkFunction = streamCodecFunction;
			return this;
		}

		/**
		 * Adds a backwards compatibility fixer used for converting from
		 * older versions of this config to the current version for
		 * any side that has been specified within the constructor.
		 *
		 * @param fixer The DataFixer to use for updating this config.
		 * @see com.mojang.datafixers.DataFixerBuilder
		 */
		public Builder<T> dataFixer(DataFixer fixer) {
			if (clientCodec != null) {
				clientFixer = fixer;
			}
			if (serverCodec != null) {
				serverFixer = fixer;
			}
			return this;
		}

		/**
		 * Adds backwards compatibility fixers used for converting from
		 * older versions of this config to the current version for both
		 * the client/integrated server and dedicated server.
		 *
		 * @param clientFixer The DataFixer to use for updating the config file on the client/integrated server.
		 * @param serverFixer The DataFixer to use for updating the config file on the dedicated server.
		 * @see com.mojang.datafixers.DataFixerBuilder
		 */
		public Builder<T> dataFixer(DataFixer clientFixer, DataFixer serverFixer) {
			this.clientFixer = clientFixer;
			this.serverFixer = serverFixer;
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
		 * @param fixer The DataFixer to use for updating this config object.
		 * @see com.mojang.datafixers.DataFixerBuilder
		 */
		@Deprecated(forRemoval = true, since = "2.0.0")
		public Builder<T> dataFixerServer(DataFixer fixer) {
			serverFixer = fixer;
			return this;
		}

		/**
		 * Adds a backwards compatibility codec used for converting from
		 * older versions of this config to the current version for
		 * the client/integrated server.
		 *
		 * @param fixer The DataFixer to use for updating this config object.
		 * @see com.mojang.datafixers.DataFixerBuilder
		 */
		@Deprecated(forRemoval = true, since = "2.0.0")
		public Builder<T> dataFixerClient(DataFixer fixer) {
			clientFixer = fixer;
			return this;
		}

		/**
		 * A callback that runs after registries have been populated.
		 *
		 * @param callback A callback that runs on registry values and the config object.
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
		 * @param callback A callback that runs on the config object.
		 */
		public Builder<T> postRegistryDepopulation(Consumer<T> callback) {
			postRegistryDepopulationCallback = callback;
			return this;
		}

		/**
		 * A shortcut to {@link GreenhouseConfigHolder.Builder#postRegistryPopulation(BiConsumer)} and {@link GreenhouseConfigHolder.Builder#postRegistryDepopulation(Consumer)} that binds/unbinds the late values of this config.
		 *
		 * @param getter A getter that should return all late values from your config object.
		 * @see LateHolder
		 * @see LateHolderSet
		 */
		public Builder<T> lateValues(Function<T, List<? extends Late>> getter, Consumer<String> onException) {
			latePopulationCallback = (lookup, config) -> getter.apply(config).forEach(late -> late.bind(lookup, onException));
			lateDepopulationCallback = (config) -> getter.apply(config).forEach(Late::unbind);
			return this;
		}

		@Deprecated(forRemoval = true, since = "2.0.0")
		public GreenhouseConfigHolder<T> buildAndRegister() {
			return build();
		}

		/**
		 * Builds this config.
		 *
		 * @return A {@link GreenhouseConfigHolder} that holds your config object.
		 */
		public GreenhouseConfigHolder<T> build() {
			if (serverCodec == null && clientCodec == null)
				throw new NullPointerException("Attempted to build config for mod " + configName + "without any associated codec.");

			if (defaultServerValue == null && defaultClientValue == null)
				throw new NullPointerException("Attempted to build config without a default value.");

			GreenhouseConfigHolderImpl<?, T> config = getGreenhouseConfigHolder();

			if (serverCodec != null)
				GreenhouseConfigHolderRegistry.registerServerConfig(configName, config);

			if (clientCodec != null)
				GreenhouseConfigHolderRegistry.registerClientConfig(configName, config);

			return config;
		}

		private @NotNull GreenhouseConfigHolderImpl<?, T> getGreenhouseConfigHolder() {
			BiConsumer<HolderLookup.Provider, T> populationCallback = latePopulationCallback != null && postRegistryPopulationCallback != null ? latePopulationCallback.andThen(postRegistryPopulationCallback) : latePopulationCallback != null ? latePopulationCallback : postRegistryPopulationCallback;
			Consumer<T> depopulationCallback = lateDepopulationCallback != null && postRegistryDepopulationCallback != null ? lateDepopulationCallback.andThen(postRegistryDepopulationCallback) : lateDepopulationCallback != null ? lateDepopulationCallback : postRegistryDepopulationCallback;

			return new GreenhouseConfigHolderImpl<>(configName, schemaVersion, configLang, defaultServerValue, defaultClientValue, serverCodec, clientCodec, networkFunction, populationCallback, depopulationCallback, serverFixer, clientFixer);
		}
	}
}
