package house.greenhouse.greenhouseconfig.impl;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import house.greenhouse.greenhouseconfig.api.GreenhouseConfigHolder;
import house.greenhouse.greenhouseconfig.api.GreenhouseConfigSide;
import house.greenhouse.greenhouseconfig.api.lang.ConfigLang;

import net.minecraft.core.HolderLookup;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
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
    private final BiConsumer<HolderLookup.Provider, T> postRegistryPopulationCallback;
    @Nullable
    private final Consumer<T> postRegistryDepopulationCallback;
    private final Map<Integer, Codec<T>> backwardsCompatCodecsServer;
    private final Map<Integer, Codec<T>> backwardsCompatCodecsClient;

    public GreenhouseConfigHolderImpl(String configName, int schemaVersion, ConfigLang<C> configLang,
                                      T defaultServerValue, T defaultClientValue,
                                      Codec<T> serverCodec, Codec<T> clientCodec,
                                      @Nullable Function<T, StreamCodec<FriendlyByteBuf, T>> networkCodecFunction,
                                      @Nullable BiConsumer<HolderLookup.Provider, T> postRegistryPopulationCallback,
                                      @Nullable Consumer<T> postRegistryDepopulationCallback,
                                      Map<Integer, Codec<T>> backwardsCompatCodecsServer,
                                      Map<Integer, Codec<T>> backwardsCompatCodecsClient) {
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
        this.backwardsCompatCodecsServer = backwardsCompatCodecsServer;
        this.backwardsCompatCodecsClient = backwardsCompatCodecsClient;
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
    public boolean isNetworkSyncable() {
        return networkCodecFunction != null;
    }

    @Override
    public T getDefaultValue() {
        return GreenhouseConfig.getPlatform().getSide() == GreenhouseConfigSide.DEDICATED_SERVER ? defaultServerValue : defaultClientValue;
    }

    public C encode(T value) {
        Codec<T> codec = GreenhouseConfig.getPlatform().getSide() == GreenhouseConfigSide.DEDICATED_SERVER ? serverCodec : clientCodec;
        return codec.encodeStart(configLang.getOps(), value).getPartialOrThrow(s -> new IllegalStateException("Failed to encode config for mod '" + this.configName + "'. " + s));
    }

    public DataResult<Pair<T, C>> decode(C value) {
        Codec<T> codec = GreenhouseConfig.getPlatform().getSide() == GreenhouseConfigSide.DEDICATED_SERVER ? serverCodec : clientCodec;
        return codec.decode(configLang.getOps(), value);
    }

    public int getSchemaVersion() {
        return this.schemaVersion;
    }

    public ConfigLang<C> getConfigLang() {
        return configLang;
    }

    public void postRegistryPopulation(HolderLookup.Provider registries, T value) {
        if (postRegistryPopulationCallback == null)
            return;
        postRegistryPopulationCallback.accept(registries, value);
    }

    public void postRegistryDepopulation(T value) {
        if (postRegistryDepopulationCallback == null)
            return;
        postRegistryDepopulationCallback.accept(value);
    }

    @Nullable
    public StreamCodec<FriendlyByteBuf, T> getNetworkCodec(T clientConfig) {
        if (networkCodecFunction == null)
            return null;
        return networkCodecFunction.apply(clientConfig);
    }

    @Nullable
    public Codec<T> getBackwardsCompatCodec(int configVersion) {
        return GreenhouseConfig.getPlatform().getSide() == GreenhouseConfigSide.DEDICATED_SERVER ? backwardsCompatCodecsServer.get(configVersion) : backwardsCompatCodecsClient.get(configVersion);
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

    @SuppressWarnings("unchecked")
    public static GreenhouseConfigHolderImpl<Object, Object> cast(GreenhouseConfigHolder<?> holder) {
        return (GreenhouseConfigHolderImpl<Object, Object>) holder;
    }
}
