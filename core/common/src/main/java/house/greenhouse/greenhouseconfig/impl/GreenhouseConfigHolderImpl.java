package house.greenhouse.greenhouseconfig.impl;

import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import house.greenhouse.greenhouseconfig.api.GreenhouseConfigHolder;
import house.greenhouse.greenhouseconfig.api.GreenhouseConfigSide;
import house.greenhouse.greenhouseconfig.api.dfu.GreenhouseConfigDFUReferences;
import house.greenhouse.greenhouseconfig.api.lang.ConfigLang;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class GreenhouseConfigHolderImpl<C, T> implements GreenhouseConfigHolder<T> {

	private final String configName;
	private final int schemaVersion;
	private final ConfigLang<C> configLang;
	private final T defaultServerValue;
	private final T defaultClientValue;
	private final Codec<T> serverCodec;
	private final Codec<T> clientCodec;
	@Nullable
	private final Function<T, StreamCodec<FriendlyByteBuf, T>> networkCodecFunction;
	@Nullable
	private final PostRegistryPopulationCallback<T> postRegistryPopulationCallback;
	@Nullable
	private final PostRegistryDepopulationCallback<T> postRegistryDepopulationCallback;
	private final DataFixer dataFixerServer;
	private final DataFixer dataFixerClient;

	public GreenhouseConfigHolderImpl(String configName, int schemaVersion, ConfigLang<C> configLang,
									  T defaultServerValue, T defaultClientValue,
									  Codec<T> serverCodec, Codec<T> clientCodec,
									  @Nullable Function<T, StreamCodec<FriendlyByteBuf, T>> networkCodecFunction,
									  @Nullable PostRegistryPopulationCallback<T> postRegistryPopulationCallback,
									  @Nullable PostRegistryDepopulationCallback<T> postRegistryDepopulationCallback,
									  DataFixer dataFixerServer,
									  DataFixer dataFixerClient) {
		this.configName = configName;
		this.schemaVersion = schemaVersion;
		this.configLang = configLang;
		this.defaultServerValue = defaultServerValue;
		this.defaultClientValue = defaultClientValue;
		this.serverCodec = serverCodec;
		this.clientCodec = clientCodec;
		this.networkCodecFunction = networkCodecFunction;
		this.postRegistryPopulationCallback = postRegistryPopulationCallback;
		this.postRegistryDepopulationCallback = postRegistryDepopulationCallback;
		this.dataFixerServer = dataFixerServer;
		this.dataFixerClient = dataFixerClient;
	}

	@SuppressWarnings("unchecked")
	public static GreenhouseConfigHolderImpl<Object, Object> cast(GreenhouseConfigHolder<?> holder) {
		return (GreenhouseConfigHolderImpl<Object, Object>) holder;
	}

	@Override
	public String getConfigName() {
		return this.configName;
	}

	@Override
	public String getConfigFileName() {
		return "config/" + configName + "." + configLang.getFileExtension();
	}

	@Override
	public boolean shouldSync() {
		return networkCodecFunction != null;
	}

	@Override
	public T getDefaultValue() {
		return GreenhouseConfig.getHelper().getSide() == GreenhouseConfigSide.DEDICATED_SERVER ? defaultServerValue : defaultClientValue;
	}

	public C encode(T value) {
		Codec<T> codec = GreenhouseConfig.getHelper().getSide() == GreenhouseConfigSide.DEDICATED_SERVER ? serverCodec : clientCodec;
		return codec.encodeStart(configLang.getOps(), value).getPartialOrThrow(s -> new IllegalStateException("Failed to encode config for mod '" + this.configName + "'. " + s));
	}

	public DataResult<Pair<T, C>> decode(C value) {
		Codec<T> codec = GreenhouseConfig.getHelper().getSide() == GreenhouseConfigSide.DEDICATED_SERVER ? serverCodec : clientCodec;
		return codec.decode(configLang.getOps(), value);
	}

	public int getSchemaVersion() {
		return this.schemaVersion;
	}

	public ConfigLang<C> getConfigLang() {
		return configLang;
	}

	public void postRegistryPopulation(HolderLookup.Provider registries, T value, boolean isClient) {
		if (postRegistryPopulationCallback == null)
			return;
		postRegistryPopulationCallback.postRegistryPopulation(registries, value, isClient);
	}

	public void postRegistryDepopulation(T value) {
		if (postRegistryDepopulationCallback == null)
			return;
		postRegistryDepopulationCallback.postRegistryDepopulation(value);
	}

	@Nullable
	public StreamCodec<FriendlyByteBuf, T> getNetworkCodec(T clientConfig) {
		if (networkCodecFunction == null)
			return null;
		return networkCodecFunction.apply(clientConfig);
	}

	@Nullable
	public C update(int previousVersion, Dynamic<C> configContents) {
		DataFixer fixer = GreenhouseConfig.getHelper().getSide() == GreenhouseConfigSide.DEDICATED_SERVER ? dataFixerServer : dataFixerClient;
		if (fixer == null)
			return null;
		return fixer.update(GreenhouseConfigDFUReferences.CONFIG, configContents, previousVersion, schemaVersion).getValue();
	}

	@Override
	public int hashCode() {
		return configName.hashCode();
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof GreenhouseConfigHolderImpl<?, ?> otherHolder)) {
			return false;
		}

		return otherHolder.configName.equals(this.configName);
	}
}
