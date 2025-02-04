package house.greenhouse.greenhouseconfig.test.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import house.greenhouse.greenhouseconfig.api.codec.GreenhouseConfigStreamCodecs;
import house.greenhouse.greenhouseconfig.api.util.Late;
import house.greenhouse.greenhouseconfig.api.util.LateHolder;
import house.greenhouse.greenhouseconfig.api.util.LateHolderSet;
import house.greenhouse.greenhouseconfig.api.codec.GreenhouseConfigCodecs;
import house.greenhouse.greenhouseconfig.test.GreenhouseConfigTest;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;

import java.util.List;

public record TestConfig(int silly,
                         LateHolder<Enchantment> favoriteEnchantment,
                         LateHolderSet<Block> redBlocks,
                         LateHolderSet<Biome> greenBiomes,
                         TextColor color,
                         ClientConfigValues clientValues) {
    public static final TagKey<Biome> GREENS = TagKey.create(Registries.BIOME, GreenhouseConfigTest.asResource("greens"));
    public static final TestConfig DEFAULT = new TestConfig(69, LateHolder.create(Enchantments.FROST_WALKER), LateHolderSet.createFromEntries(Registries.BLOCK, List.of(ResourceKey.create(Registries.BLOCK, ResourceLocation.withDefaultNamespace("netherrack")))), LateHolderSet.createMixed(Registries.BIOME, List.of(GREENS), List.of(Biomes.BAMBOO_JUNGLE)), TextColor.parseColor("#0095a8").getOrThrow(), ClientConfigValues.DEFAULT);

    public static final Codec<TestConfig> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            GreenhouseConfigCodecs.defaultFieldCodec(GreenhouseConfigCodecs.commentedCodec(Codec.INT, "The value which makes this config very silly."), "silly", DEFAULT.silly()).forGetter(TestConfig::silly),
            GreenhouseConfigCodecs.defaultFieldCodec(GreenhouseConfigCodecs.commentedCodec(GreenhouseConfigCodecs.lateHolderCodec(Registries.ENCHANTMENT), "Your favorite enchantment.", "Note: Pug was not biased when she set it to hers."), "favorite_enchantment", DEFAULT.favoriteEnchantment()).forGetter(TestConfig::favoriteEnchantment),
            GreenhouseConfigCodecs.defaultFieldCodec(GreenhouseConfigCodecs.commentedCodec(GreenhouseConfigCodecs.lateHolderSetCodec(Registries.BLOCK), "One block, two block, red block, blue block."), "red_blocks", DEFAULT.redBlocks()).forGetter(TestConfig::redBlocks),
            GreenhouseConfigCodecs.defaultFieldCodec(GreenhouseConfigCodecs.commentedCodec(GreenhouseConfigCodecs.lateHolderSetCodec(Registries.BIOME), "Biomes that are green", "This is an extra line to show how green they really are!"), "green_biomes", DEFAULT.greenBiomes()).forGetter(TestConfig::greenBiomes),
            GreenhouseConfigCodecs.defaultFieldCodec(GreenhouseConfigCodecs.commentedCodec(TextColor.CODEC, "This is a value that exists on both the client and server."), "color", DEFAULT.color()).forGetter(TestConfig::color),
            ClientConfigValues.CODEC.forGetter(TestConfig::clientValues)
    ).apply(inst, TestConfig::new));

    public static StreamCodec<FriendlyByteBuf, TestConfig> streamCodec(TestConfig clientConfig) {
        return StreamCodec.composite(
                ByteBufCodecs.INT,
                TestConfig::silly,
                GreenhouseConfigStreamCodecs.lateHolderStreamCodec(Registries.ENCHANTMENT),
                TestConfig::favoriteEnchantment,
                GreenhouseConfigStreamCodecs.lateHolderSetStreamCodec(Registries.BLOCK),
                TestConfig::redBlocks,
                GreenhouseConfigStreamCodecs.lateHolderSetStreamCodec(Registries.BIOME),
                TestConfig::greenBiomes,
                ByteBufCodecs.fromCodec(TextColor.CODEC),
                TestConfig::color,
                (t1, t2, t3, t4, t5) -> new TestConfig(t1, t2, t3, t4, t5, clientConfig.clientValues())
        );
    }

    public List<? extends Late> getLateValues() {
        return List.of(favoriteEnchantment, redBlocks, greenBiomes);
    }

    public record ClientConfigValues(TextColor color) {
        public static final ClientConfigValues DEFAULT = new ClientConfigValues(TextColor.parseColor("#54bf6b").getOrThrow());

        public static final MapCodec<ClientConfigValues> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                GreenhouseConfigCodecs.defaultFieldCodec(GreenhouseConfigCodecs.commentedCodec(TextColor.CODEC, "This is a value that only exists on the client."), "client_color", DEFAULT.color()).forGetter(ClientConfigValues::color)
        ).apply(inst, ClientConfigValues::new));
    }

    public static class CompatCodecs {
        public static final Codec<TestConfig> V2 = RecordCodecBuilder.create(inst -> inst.group(
                GreenhouseConfigCodecs.defaultFieldCodec(Codec.INT, "silly", DEFAULT.silly()).forGetter(TestConfig::silly),
                GreenhouseConfigCodecs.defaultFieldCodec(GreenhouseConfigCodecs.commentedCodec(GreenhouseConfigCodecs.lateHolderCodec(Registries.ENCHANTMENT), "Your favorite enchantment.", "Note: Pug was not biased when she set it to hers."), "favorite_enchantment", DEFAULT.favoriteEnchantment()).forGetter(TestConfig::favoriteEnchantment),
                GreenhouseConfigCodecs.defaultFieldCodec(GreenhouseConfigCodecs.commentedCodec(GreenhouseConfigCodecs.lateHolderSetCodec(Registries.BLOCK), "One block, two block, red block, blue block."), "red_blocks", DEFAULT.redBlocks()).forGetter(TestConfig::redBlocks),
                GreenhouseConfigCodecs.defaultFieldCodec(GreenhouseConfigCodecs.commentedCodec(GreenhouseConfigCodecs.lateHolderSetCodec(Registries.BIOME), "Biomes that are green", "This is an extra line to show how green they really are!"), "green_biomes", DEFAULT.greenBiomes()).forGetter(TestConfig::greenBiomes)
        ).apply(inst, (t1, t2, t3, t4) -> new TestConfig(t1, t2, t3, t4, DEFAULT.color(), DEFAULT.clientValues())));
        public static final Codec<TestConfig> V1 = RecordCodecBuilder.create(inst -> inst.group(
                GreenhouseConfigCodecs.defaultFieldCodec(Codec.INT, "silly", DEFAULT.silly()).forGetter(TestConfig::silly),
                GreenhouseConfigCodecs.defaultFieldCodec(GreenhouseConfigCodecs.lateHolderSetCodec(Registries.BIOME), "green_biomes", DEFAULT.greenBiomes()).forGetter(TestConfig::greenBiomes)
        ).apply(inst, (t1, t2) -> new TestConfig(t1, DEFAULT.favoriteEnchantment(), DEFAULT.redBlocks(), t2, DEFAULT.color(), DEFAULT.clientValues())));
    }
}
