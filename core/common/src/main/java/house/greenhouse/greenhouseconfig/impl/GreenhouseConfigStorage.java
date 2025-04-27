package house.greenhouse.greenhouseconfig.impl;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;

import house.greenhouse.greenhouseconfig.api.lang.ConfigLang;
import house.greenhouse.greenhouseconfig.api.GreenhouseConfigHolder;
import house.greenhouse.greenhouseconfig.api.GreenhouseConfigSide;
import house.greenhouse.greenhouseconfig.impl.network.SyncGreenhouseConfigPacket;
import net.minecraft.core.HolderLookup;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@SuppressWarnings("unchecked")
public class GreenhouseConfigStorage {

    private static final Map<GreenhouseConfigHolder<?>, Object> SERVER_CONFIGS = new HashMap<>();
    private static final Map<GreenhouseConfigHolder<?>, Object> CLIENT_CONFIGS = new HashMap<>();
    private static final Map<GreenhouseConfigHolder<?>, Object> UNSYNCED_CLIENT_CONFIGS = new HashMap<>();

    public static <C, T> T getConfig(GreenhouseConfigHolderImpl<C, T> holder, boolean unsynced) {
        boolean isServer = GreenhouseConfig.getPlatform().getSide() == GreenhouseConfigSide.DEDICATED_SERVER;
        if (isServer && !SERVER_CONFIGS.containsKey(holder) || !isServer && (unsynced ? !UNSYNCED_CLIENT_CONFIGS.containsKey(holder) : !CLIENT_CONFIGS.containsKey(holder))) {
            throw new NullPointerException("Could not find config '" + holder.getConfigName() + "'.");
        }
        return isServer ? (T) SERVER_CONFIGS.get(holder) : unsynced ? (T) UNSYNCED_CLIENT_CONFIGS.get(holder) : (T) CLIENT_CONFIGS.get(holder);
    }

    public static Set<GreenhouseConfigHolder<?>> getConfigs() {
        boolean isServer = GreenhouseConfig.getPlatform().getSide() == GreenhouseConfigSide.DEDICATED_SERVER;
        return isServer ? SERVER_CONFIGS.keySet() : CLIENT_CONFIGS.keySet();
    }

    public static <T> void updateConfig(GreenhouseConfigHolder<T> holder, T value) {
        boolean isServer = GreenhouseConfig.getPlatform().getSide() == GreenhouseConfigSide.DEDICATED_SERVER;
        if (isServer && !SERVER_CONFIGS.containsKey(holder) || !isServer && !CLIENT_CONFIGS.containsKey(holder))
            throw new UnsupportedOperationException("Can only update config '" + holder.getConfigName() + "' after the initial config loading stage.");

        if (isServer)
            SERVER_CONFIGS.put(holder, value);
        else
            CLIENT_CONFIGS.put(holder, value);
    }

    public static Collection<SyncGreenhouseConfigPacket> createSyncPackets() {
        ImmutableList.Builder<SyncGreenhouseConfigPacket> list = ImmutableList.builder();
        var map = GreenhouseConfig.getPlatform().getSide() == GreenhouseConfigSide.DEDICATED_SERVER ? SERVER_CONFIGS : CLIENT_CONFIGS;
        for (Map.Entry<GreenhouseConfigHolder<?>, Object> entry : map.entrySet()) {
            var networkCodec = GreenhouseConfigHolderImpl.cast(entry.getKey()).getNetworkCodec(entry.getKey().get());
            if (networkCodec != null)
                list.add(new SyncGreenhouseConfigPacket(entry.getKey().getConfigName(), entry.getValue()));
        }
        return list.build();
    }

    public static <C, T> T reloadConfig(GreenhouseConfigHolderImpl<C, T> holder, Consumer<String> onError) {
        File file = GreenhouseConfig.getPlatform().getConfigPath().resolve(holder.getConfigName() + "." + holder.getConfigLang().getFileExtension()).toFile();
        try {
            var lang = holder.getConfigLang();
            var json = lang.read(new FileReader(file));
            var value = holder.decode(json);
            if (value.isError()) {
                onError.accept(value.error().orElseThrow().message());
                return null;
            }
            if (value.isError() && value.hasResultOrPartial()) {
                createConfig(holder, value.getPartialOrThrow().getFirst(), file);
                onError.accept(value.error().orElseThrow().message());
            }
            if (GreenhouseConfig.getPlatform().getSide() == GreenhouseConfigSide.DEDICATED_SERVER)
                SERVER_CONFIGS.put(holder, value.getPartialOrThrow().getFirst());
            else {
                T config = value.getPartialOrThrow().getFirst();
                CLIENT_CONFIGS.put(holder, config);
                UNSYNCED_CLIENT_CONFIGS.put(holder, config);
            }
            return value.getPartialOrThrow().getFirst();
        } catch (Exception ex) {
            onError.accept(ex.toString());
        }
        return null;
    }

    public static void generateServerConfigs() {
        for (GreenhouseConfigHolder<?> config : GreenhouseConfigHolderRegistry.SERVER_CONFIG_HOLDERS.values()) {
            var holder = GreenhouseConfigHolderImpl.cast(config);
            loadConfig(holder, SERVER_CONFIGS::put);
            GreenhouseConfig.getPlatform().postLoadEvent(holder, holder.get(), GreenhouseConfigSide.DEDICATED_SERVER);
        }
    }

    public static void generateClientConfigs() {
        for (GreenhouseConfigHolder<?> config : GreenhouseConfigHolderRegistry.CLIENT_CONFIG_HOLDERS.values()) {
            var holder = GreenhouseConfigHolderImpl.cast(config);
            loadConfig(holder, (confHolder, conf) -> {
                CLIENT_CONFIGS.put(confHolder, conf);
                UNSYNCED_CLIENT_CONFIGS.put(confHolder, conf);
            });
            GreenhouseConfig.getPlatform().postLoadEvent(holder, holder.get(), GreenhouseConfigSide.CLIENT);
        }
    }

    public static void onRegistryPopulation(HolderLookup.Provider registries) {
        boolean isServer = GreenhouseConfig.getPlatform().getSide() == GreenhouseConfigSide.DEDICATED_SERVER;
        Map<GreenhouseConfigHolder<?>, Object> configs = isServer ? SERVER_CONFIGS : CLIENT_CONFIGS;
        for (Map.Entry<GreenhouseConfigHolder<?>, Object> entry : configs.entrySet()) {
            GreenhouseConfigHolderImpl.cast(entry.getKey()).postRegistryPopulation(registries, entry.getValue());
            GreenhouseConfig.getPlatform().postPopulationEvent((GreenhouseConfigHolder<Object>) entry.getKey(), entry.getValue(), GreenhouseConfig.getPlatform().getSide());
        }
    }

    public static void onRegistryDepopulation() {
        boolean isServer = GreenhouseConfig.getPlatform().getSide() == GreenhouseConfigSide.DEDICATED_SERVER;
        Map<GreenhouseConfigHolder<?>, Object> configs = isServer ? SERVER_CONFIGS : CLIENT_CONFIGS;
        for (Map.Entry<GreenhouseConfigHolder<?>, Object> entry : configs.entrySet()) {
            GreenhouseConfigHolderImpl.cast(entry.getKey()).postRegistryDepopulation(entry.getValue());
            GreenhouseConfig.getPlatform().postDepopulationEvent((GreenhouseConfigHolder<Object>) entry.getKey(), entry.getValue(), GreenhouseConfig.getPlatform().getSide());
        }
    }

    public static void individualRegistryPopulation(HolderLookup.Provider registries, GreenhouseConfigHolder<?> holder) {
        individualRegistryPopulation(registries, holder, holder.get());
    }

    public static void individualRegistryPopulation(HolderLookup.Provider registries, GreenhouseConfigHolder<?> holder, Object value) {
        GreenhouseConfigHolderImpl.cast(holder).postRegistryPopulation(registries, value);
        GreenhouseConfig.getPlatform().postPopulationEvent((GreenhouseConfigHolder<Object>) holder, value, GreenhouseConfig.getPlatform().getSide());
    }

    private static <C, T> void loadConfig(GreenhouseConfigHolderImpl<C, T> holder, BiConsumer<GreenhouseConfigHolder<?>, Object> consumer) {
        File file = GreenhouseConfig.getPlatform().getConfigPath().resolve(holder.getConfigName() + "." + holder.getConfigLang().getFileExtension()).toFile();

        if (file.exists()) {
            try {
                ConfigLang<C> lang = holder.getConfigLang();
                C contents = lang.read(new FileReader(file));
                int schemaVersion = readSchemaVersion(file, holder);
                if (schemaVersion != holder.getSchemaVersion()) {
                    @Nullable C converted = holder.update(schemaVersion, new Dynamic<>(holder.getConfigLang().getOps(), contents));
                    if (converted != null) {
                        var dataResult = holder.decode(converted);
                        if (!dataResult.hasResultOrPartial()) {
                            GreenhouseConfig.LOG.error("Could not decode old config file '{}'. Using default instead. {}", file.getPath(), dataResult.error().orElseThrow().message());
                        } else {
                            T value = createConfig(holder, dataResult.resultOrPartial(string -> GreenhouseConfig.LOG.error("Could not completely decode old config file '{}'. Using partially decoded value. {}", file.getPath(), dataResult.error().orElseThrow().message())).orElseThrow().getFirst(), file);
                            consumer.accept(holder, value);
                            return;
                        }
                    }
                } else {
                    var dataResult = holder.decode(contents);
                    if (!dataResult.hasResultOrPartial()) {
                        GreenhouseConfig.LOG.error("Could not decode config file '{}'. Using default instead. {}", file.getPath(), dataResult.error().orElseThrow().message());
                        T config = createConfig(holder, holder.getDefaultValue(), file);
                        consumer.accept(holder, config);
                        return;
                    }

                    consumer.accept(holder, dataResult.resultOrPartial(string -> GreenhouseConfig.LOG.error("Could not completely decode config file '{}'. Using partially decoded value. {}", file.getPath(), dataResult.error().orElseThrow())).orElseThrow().getFirst());
                    return;
                }
            } catch (Exception ex) {
                GreenhouseConfig.LOG.error("Could not decode config file '{}'.", file.getPath(), ex);
            }
        }

        consumer.accept(holder, holder.getDefaultValue());
        saveConfig(holder, holder.getDefaultValue());
    }

    public static <T> void saveConfig(GreenhouseConfigHolder<T> holder, T config) {
        saveConfig((GreenhouseConfigHolderImpl<?, T>) holder, config);
    }

    private static <C, T> void saveConfig(GreenhouseConfigHolderImpl<C, T> holder, T config) {
        try {
            int folderCount = holder.getConfigName().split("/").length;

            if (folderCount > 1) {
                String folderName = holder.getConfigName().substring(0, holder.getConfigName().lastIndexOf("/"));
                Path path = GreenhouseConfig.getPlatform().getConfigPath().resolve(folderName);
                Files.createDirectories(path);
            }

            File file = GreenhouseConfig.getPlatform().getConfigPath().resolve(holder.getConfigName() + "." + holder.getConfigLang().getFileExtension()).toFile();
            if (!file.exists())
                Files.createFile(file.toPath());
            createConfig(holder, config, file);
        } catch (IOException ex) {
            GreenhouseConfig.LOG.error("Failed to create config for mod '{}' to config directory. Skipping and using default config values.", holder.getConfigName(), ex);
        }
    }

    private static <C, T> T createConfig(GreenhouseConfigHolderImpl<C, T> holder, T config, File file) throws IOException {
        DynamicOps<C> ops = holder.getConfigLang().getOps();
        C element = holder.encode(config);

        if (ops.getMap(element).isError()) {
            element = ops.createMap(Map.of(ops.createString("value"), element));
        }

        try (FileWriter writer = new FileWriter(file)) {
            holder.getConfigLang().write(writer, element);
            writeSchemaVersion(file, holder);
        } catch (Exception ex) {
            GreenhouseConfig.LOG.error("Failed to write config '{}'.", holder.getConfigName(), ex);
            return null;
        }

        return holder.decode(element).getOrThrow().getFirst();
    }

    private static void writeSchemaVersion(File file, GreenhouseConfigHolder<?> holder) throws IOException {
        Files.setAttribute(file.toPath(), "user:GreenhouseConfigSchemaVersion", ByteBuffer.wrap(String.valueOf(holder.getSchemaVersion()).getBytes(StandardCharsets.UTF_8)));
    }

    private static int readSchemaVersion(File file, GreenhouseConfigHolder<?> holder) {
        try {
            UserDefinedFileAttributeView view = Files.getFileAttributeView(file.toPath(), UserDefinedFileAttributeView.class);
            ByteBuffer buffer = ByteBuffer.allocate(view.size("GreenhouseConfigSchemaVersion"));
            view.read("GreenhouseConfigSchemaVersion", buffer);
            buffer.flip();
            return Integer.parseInt(StandardCharsets.UTF_8.decode(buffer).toString());
        } catch (IOException e) {
            // Return the current schema version in the case of being unable to read the file attribute.
            // Maybe consider a fallback schema version for migrating from other config libs.
            return holder.getSchemaVersion();
        }
    }
}
