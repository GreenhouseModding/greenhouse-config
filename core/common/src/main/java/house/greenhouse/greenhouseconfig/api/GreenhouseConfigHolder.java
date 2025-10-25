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
		return GreenhouseConfig.getHelper().queryConfig(this);
	}

	/**
	 * Syncs the config from the server to all clients.
	 *
	 * @param server The server to sync from.
	 */
	default void syncConfig(MinecraftServer server) {
		for (ServerPlayer player : server.getPlayerList().getPlayers())
			GreenhouseConfig.getHelper().syncConfig(this, server, player);
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
		private final Codec<T> serverCodec;
		@Nullable
		private final Codec<T> clientCodec;

		@Nullable
		private final T defaultServerValue;
		@Nullable
		private final T defaultClientValue;
		@Nullable
		private Function<T, StreamCodec<FriendlyByteBuf, T>> networkFunction;

		@Nullable
		private PostRegistryPopulationCallback<T> latePopulationCallback;
		@Nullable
		private PostRegistryDepopulationCallback<T> lateDepopulationCallback;

		@Nullable
		private PostRegistryPopulationCallback<T> postRegistryPopulationCallback;
		@Nullable
		private PostRegistryDepopulationCallback<T> postRegistryDepopulationCallback;

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
		 * A callback that runs after registries have been populated.
		 *
		 * @param callback A callback that runs on registry values and the config object.
		 */
		public Builder<T> postRegistryPopulation(PostRegistryPopulationCallback<T> callback) {
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
		public Builder<T> postRegistryDepopulation(PostRegistryDepopulationCallback<T> callback) {
			postRegistryDepopulationCallback = callback;
			return this;
		}

		/**
		 * @deprecated Use {@link GreenhouseConfigHolder.Builder#postRegistryPopulation(PostRegistryPopulationCallback)} instead for potential client context.
		 */
		@Deprecated(forRemoval = true, since = "2.4.0")
		public Builder<T> postRegistryPopulation(BiConsumer<HolderLookup.Provider, T> callback) {
			postRegistryPopulationCallback = ((registries, config, isClient) -> callback.accept(registries, config));
			return this;
		}

		/**
		 * A shortcut to {@link GreenhouseConfigHolder.Builder#postRegistryPopulation(PostRegistryPopulationCallback)} and {@link GreenhouseConfigHolder.Builder#postRegistryDepopulation(PostRegistryDepopulationCallback)} that binds/unbinds the late values of this config.
		 * <p>
		 * You will be better off using something else if you need client-specific values.
		 *
		 * @param getter A getter that should return all late values from your config object.
		 * @see LateHolder
		 * @see LateHolderSet
		 */
		public Builder<T> lateValues(Function<T, List<? extends Late>> getter, Consumer<String> onException) {
			latePopulationCallback = (lookup, config, isClient) -> getter.apply(config).forEach(late -> late.bind(lookup, onException));
			lateDepopulationCallback = (config) -> getter.apply(config).forEach(Late::unbind);
			return this;
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
			PostRegistryPopulationCallback<T> populationCallback = latePopulationCallback != null && postRegistryPopulationCallback != null ? latePopulationCallback.andThen(postRegistryPopulationCallback) : latePopulationCallback != null ? latePopulationCallback : postRegistryPopulationCallback;
			PostRegistryDepopulationCallback<T> depopulationCallback = lateDepopulationCallback != null && postRegistryDepopulationCallback != null ? lateDepopulationCallback.andThen(postRegistryDepopulationCallback) : lateDepopulationCallback != null ? lateDepopulationCallback : postRegistryDepopulationCallback;

			return new GreenhouseConfigHolderImpl<>(configName, schemaVersion, configLang, defaultServerValue, defaultClientValue, serverCodec, clientCodec, networkFunction, populationCallback, depopulationCallback, serverFixer, clientFixer);
		}
	}

	@FunctionalInterface
	public interface PostRegistryPopulationCallback<T> {
		void postRegistryPopulation(HolderLookup.Provider registries, T config, boolean isClient);

		default PostRegistryPopulationCallback<T> andThen(PostRegistryPopulationCallback<T> callback) {
			return (registries, config, isClient) -> {
				this.postRegistryPopulation(registries, config, isClient);
				callback.postRegistryPopulation(registries, config, isClient);
			};
		}
	}

	@FunctionalInterface
	public interface PostRegistryDepopulationCallback<T> {
		void postRegistryDepopulation(T config);

		default PostRegistryDepopulationCallback<T> andThen(PostRegistryDepopulationCallback<T> callback) {
			return (config) -> {
				this.postRegistryDepopulation(config);
				callback.postRegistryDepopulation(config);
			};
		}
	}
}
