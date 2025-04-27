package house.greenhouse.greenhouseconfig.test.config;

import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.DataFixerBuilder;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import house.greenhouse.greenhouseconfig.api.lang.CommentedValue;
import house.greenhouse.greenhouseconfig.api.util.DefaultFieldUtil;
import house.greenhouse.greenhouseconfig.api.util.Late;
import house.greenhouse.greenhouseconfig.api.util.LateHolder;
import house.greenhouse.greenhouseconfig.api.util.LateHolderSet;
import house.greenhouse.greenhouseconfig.test.GreenhouseConfigTest;
import house.greenhouse.greenhouseconfig.test.dfu.fix.V1ToV2FieldsFix;
import house.greenhouse.greenhouseconfig.test.dfu.fix.V2ToV3FieldsFix;
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
	public static final TestConfig DEFAULT = new TestConfig(
			69,
			Pair.of(LateHolder.create(Enchantments.FROST_WALKER), Opinion.LIKE),
			LateHolderSet.builder(Registries.BLOCK)
					.add(ResourceKey.create(Registries.BLOCK, ResourceLocation.withDefaultNamespace("netherrack")))
					.build(),
			LateHolderSet.builder(Registries.BIOME)
					.add(GREENS)
					.add(Biomes.BAMBOO_JUNGLE)
					.build(),
			TextColor.parseColor("#0095a8").getOrThrow(),
			ClientConfigValues.DEFAULT
	);

	public static final Codec<Pair<LateHolder<Enchantment>, Opinion>> ENCHANTMENT_OPINION_CODEC = RecordCodecBuilder.create(inst -> inst.group(
			CommentedValue.codec(LateHolder.codec(Registries.ENCHANTMENT), "The enchantment you wish to provide an opinion on.").fieldOf("enchantment").forGetter(Pair::getFirst),
			CommentedValue.codec(Opinion.CODEC, "The opinion you have on the above enchantment.", "Can either be 'like' or 'dislike' (case-sensitive)").fieldOf("opinion").forGetter(Pair::getSecond)
	).apply(inst, Pair::of));
	public static final Codec<TestConfig> CODEC = RecordCodecBuilder.create(inst -> inst.group(
			DefaultFieldUtil.codecWithComments(Codec.INT, "silly", DEFAULT.silly(), "The value which makes this config very silly.")
					.forGetter(TestConfig::silly),
			DefaultFieldUtil.codecWithComments(ENCHANTMENT_OPINION_CODEC, "enchantment_opinion", DEFAULT.enchantmentOpinion(), "An enchantment that you either like or dislike.", "Note: Calico was not biased here.")
					.forGetter(TestConfig::enchantmentOpinion),
			DefaultFieldUtil.codecWithComments(LateHolderSet.codec(Registries.BLOCK), "red_blocks", DEFAULT.redBlocks(), "One block, two block, red block, blue block.")
					.forGetter(TestConfig::redBlocks),
			DefaultFieldUtil.codecWithComments(LateHolderSet.codec(Registries.BIOME), "green_biomes", DEFAULT.greenBiomes(), "Biomes that are green", "This is an extra line to show how green they really are!").forGetter(TestConfig::greenBiomes),
			DefaultFieldUtil.codecWithComments(TextColor.CODEC, "color", DEFAULT.color(), "This is a value that exists on both the client and server.")
					.forGetter(TestConfig::color),
			ClientConfigValues.CODEC.forGetter(TestConfig::clientValues)
	).apply(inst, TestConfig::new));

	public static StreamCodec<FriendlyByteBuf, TestConfig> streamCodec(TestConfig clientConfig) {
		return StreamCodec.composite(
				ByteBufCodecs.INT,
				TestConfig::silly,
				LateHolder.streamCodec(Registries.ENCHANTMENT),
				config -> config.enchantmentOpinion().getFirst(),
				Opinion.STREAM_CODEC,
				config -> config.enchantmentOpinion().getSecond(),
				LateHolderSet.streamCodec(Registries.BLOCK),
				TestConfig::redBlocks,
				LateHolderSet.streamCodec(Registries.BIOME),
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
				DefaultFieldUtil.codecWithComments(TextColor.CODEC, "client_color", DEFAULT.color(), "This is a value that only exists on the client.")
						.forGetter(ClientConfigValues::color)
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
				DefaultFieldUtil.codecWithComments(Codec.INT, "silly", DEFAULT.silly(), "The value which makes this config very silly.")
						.forGetter(TestConfig::silly),
				DefaultFieldUtil.codecWithComments(LateHolder.codec(Registries.ENCHANTMENT), "liked_enchantment", DEFAULT.enchantmentOpinion().getFirst(), "An enchantment you like.", "Note: Calico was not biased here.")
						.forGetter(config -> config.enchantmentOpinion().getFirst()),
				DefaultFieldUtil.codecWithComments(LateHolderSet.codec(Registries.BLOCK), "blue_blocks", DEFAULT.redBlocks(), "One block, two block, red block, blue block.")
						.forGetter(TestConfig::redBlocks),
				DefaultFieldUtil.codecWithComments(LateHolderSet.codec(Registries.BIOME), "green_biomes", DEFAULT.greenBiomes(), "Biomes that are green", "This is an extra line to show how green they really are!")
						.forGetter(TestConfig::greenBiomes)
		).apply(inst, (t1, t2, t3, t4) -> new TestConfig(t1, Pair.of(t2, Opinion.LIKE), t3, t4, DEFAULT.color(), DEFAULT.clientValues())));
		public static final Codec<TestConfig> V1 = RecordCodecBuilder.create(inst -> inst.group(
				DefaultFieldUtil.codec(Codec.INT, "funny", DEFAULT.silly()).forGetter(TestConfig::silly),
				DefaultFieldUtil.codec(LateHolderSet.codec(Registries.BIOME), "green_biomes", DEFAULT.greenBiomes()).forGetter(TestConfig::greenBiomes)
		).apply(inst, (t1, t2) -> new TestConfig(t1, DEFAULT.enchantmentOpinion(), DEFAULT.redBlocks(), t2, DEFAULT.color(), DEFAULT.clientValues())));
	}
}
