package house.greenhouse.greenhouseconfig.test.config;

import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.DataFixerBuilder;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import house.greenhouse.greenhouseconfig.api.codec.GreenhouseConfigStreamCodecs;
import house.greenhouse.greenhouseconfig.api.util.Late;
import house.greenhouse.greenhouseconfig.api.util.LateHolder;
import house.greenhouse.greenhouseconfig.api.util.LateHolderSet;
import house.greenhouse.greenhouseconfig.api.codec.GreenhouseConfigCodecs;
import house.greenhouse.greenhouseconfig.test.GreenhouseConfigTest;
import house.greenhouse.greenhouseconfig.test.dfu.fix.V2ToV3FieldsFix;
import house.greenhouse.greenhouseconfig.test.dfu.fix.V1ToV2FieldsFix;
import house.greenhouse.greenhouseconfig.test.dfu.schema.EmptySchema;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.IntFunction;

public record TestConfig(int silly,
                         Pair<LateHolder<Enchantment>, Opinion> enchantmentOpinion,
                         LateHolderSet<Block> redBlocks,
                         LateHolderSet<Biome> greenBiomes,
                         TextColor color,
                         ClientConfigValues clientValues) {
    public static final TagKey<Biome> GREENS = TagKey.create(Registries.BIOME, GreenhouseConfigTest.asResource("greens"));
    public static final TestConfig DEFAULT = new TestConfig(69, Pair.of(LateHolder.create(Enchantments.FROST_WALKER), Opinion.LIKE), LateHolderSet.createFromEntries(Registries.BLOCK, List.of(ResourceKey.create(Registries.BLOCK, ResourceLocation.withDefaultNamespace("netherrack")))), LateHolderSet.createMixed(Registries.BIOME, List.of(GREENS), List.of(Biomes.BAMBOO_JUNGLE)), TextColor.parseColor("#0095a8").getOrThrow(), ClientConfigValues.DEFAULT);

    public static final Codec<Pair<LateHolder<Enchantment>, Opinion>> ENCHANTMENT_OPINION_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            GreenhouseConfigCodecs.commentedCodec(GreenhouseConfigCodecs.lateHolderCodec(Registries.ENCHANTMENT), "The enchantment you wish to provide an opinion on.").fieldOf("enchantment").forGetter(Pair::getFirst),
            GreenhouseConfigCodecs.commentedCodec(Opinion.CODEC, "The opinion you have on the above enchantment.", "Can either be 'like' or 'dislike' (case-sensitive)").fieldOf("opinion").forGetter(Pair::getSecond)
    ).apply(inst, Pair::of));

    public static final Codec<TestConfig> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            GreenhouseConfigCodecs.defaultFieldCodec(GreenhouseConfigCodecs.commentedCodec(Codec.INT, "The value which makes this config very silly."), "silly", DEFAULT.silly()).forGetter(TestConfig::silly),
            GreenhouseConfigCodecs.defaultFieldCodec(GreenhouseConfigCodecs.commentedCodec(ENCHANTMENT_OPINION_CODEC, "An enchantment that you either like or dislike.", "Note: Pug was not biased here."), "enchantment_opinion", DEFAULT.enchantmentOpinion()).forGetter(TestConfig::enchantmentOpinion),
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
                config -> config.enchantmentOpinion().getFirst(),
                Opinion.STREAM_CODEC,
                config -> config.enchantmentOpinion().getSecond(),
                GreenhouseConfigStreamCodecs.lateHolderSetStreamCodec(Registries.BLOCK),
                TestConfig::redBlocks,
                GreenhouseConfigStreamCodecs.lateHolderSetStreamCodec(Registries.BIOME),
                TestConfig::greenBiomes,
                ByteBufCodecs.fromCodec(TextColor.CODEC),
                TestConfig::color,
                (t1, t2, t3, t4, t5, t6) -> new TestConfig(t1, Pair.of(t2, t3), t4, t5, t6, clientConfig.clientValues())
        );
    }

    public List<? extends Late> getLateValues() {
        return List.of(enchantmentOpinion.getFirst(), redBlocks, greenBiomes);
    }

    public enum Opinion implements StringRepresentable {
        LIKE("like", 0),
        DISLIKE("dislike", 1);

        public static final Codec<Opinion> CODEC = StringRepresentable.fromEnum(Opinion::values);
        public static final IntFunction<Opinion> BY_ID = ByIdMap.continuous(Opinion::id, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
        public static final StreamCodec<ByteBuf, Opinion> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Opinion::id);

        final String name;
        final int id;

        Opinion(String name, int id) {
            this.name = name;
            this.id = id;
        }

        @Override
        public @NotNull String getSerializedName() {
            return name;
        }

        private int id() {
            return id;
        }
    }

    public record ClientConfigValues(TextColor color) {
        public static final ClientConfigValues DEFAULT = new ClientConfigValues(TextColor.parseColor("#54bf6b").getOrThrow());

        public static final MapCodec<ClientConfigValues> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                GreenhouseConfigCodecs.defaultFieldCodec(GreenhouseConfigCodecs.commentedCodec(TextColor.CODEC, "This is a value that only exists on the client."), "client_color", DEFAULT.color()).forGetter(ClientConfigValues::color)
        ).apply(inst, ClientConfigValues::new));
    }

    public static class Fixer {
        public static final DataFixer INSTANCE = createFixer();

        private static DataFixer createFixer() {
            DataFixerBuilder builder = new DataFixerBuilder(3);
            builder.addSchema(1, EmptySchema::new);
            Schema v2 = builder.addSchema(2, Schema::new);
            builder.addFixer(new V1ToV2FieldsFix(v2));
            Schema v3 = builder.addSchema(3, Schema::new);
            builder.addFixer(new V2ToV3FieldsFix(v3));
            return builder.build().fixer();
        }
    }

    /**
     * Purely used for testing the fixer.
     * You'll only need the fixer if you are implementing backwards compatibility with a previous config version.
     */
    public static class PreviousVersionCodecs {
        public static final Codec<TestConfig> V2 = RecordCodecBuilder.create(inst -> inst.group(
                GreenhouseConfigCodecs.defaultFieldCodec(GreenhouseConfigCodecs.commentedCodec(Codec.INT, "The value which makes this config very silly."), "silly", DEFAULT.silly()).forGetter(TestConfig::silly),
                GreenhouseConfigCodecs.defaultFieldCodec(GreenhouseConfigCodecs.commentedCodec(GreenhouseConfigCodecs.lateHolderCodec(Registries.ENCHANTMENT), "An enchantment you like.", "Note: Pug was not biased here."), "liked_enchantment", DEFAULT.enchantmentOpinion().getFirst()).forGetter(config -> config.enchantmentOpinion().getFirst()),
                GreenhouseConfigCodecs.defaultFieldCodec(GreenhouseConfigCodecs.commentedCodec(GreenhouseConfigCodecs.lateHolderSetCodec(Registries.BLOCK), "One block, two block, red block, blue block."), "blue_blocks", DEFAULT.redBlocks()).forGetter(TestConfig::redBlocks),
                GreenhouseConfigCodecs.defaultFieldCodec(GreenhouseConfigCodecs.commentedCodec(GreenhouseConfigCodecs.lateHolderSetCodec(Registries.BIOME), "Biomes that are green", "This is an extra line to show how green they really are!"), "green_biomes", DEFAULT.greenBiomes()).forGetter(TestConfig::greenBiomes)
        ).apply(inst, (t1, t2, t3, t4) -> new TestConfig(t1, Pair.of(t2, Opinion.LIKE), t3, t4, DEFAULT.color(), DEFAULT.clientValues())));
        public static final Codec<TestConfig> V1 = RecordCodecBuilder.create(inst -> inst.group(
                GreenhouseConfigCodecs.defaultFieldCodec(Codec.INT, "funny", DEFAULT.silly()).forGetter(TestConfig::silly),
                GreenhouseConfigCodecs.defaultFieldCodec(GreenhouseConfigCodecs.lateHolderSetCodec(Registries.BIOME), "green_biomes", DEFAULT.greenBiomes()).forGetter(TestConfig::greenBiomes)
        ).apply(inst, (t1, t2) -> new TestConfig(t1, DEFAULT.enchantmentOpinion(), DEFAULT.redBlocks(), t2, DEFAULT.color(), DEFAULT.clientValues())));
    }
}
