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

public record SplitConfig(CommonValues common,
						  ClientValues client) {
	public static final TagKey<Biome> GREENS = TagKey.create(Registries.BIOME, GreenhouseConfigTest.asResource("greens"));

	public static final SplitConfig SERVER_DEFAULT = new SplitConfig(
			CommonValues.DEFAULT,
			null
	);
	public static final SplitConfig CLIENT_DEFAULT = new SplitConfig(
			CommonValues.DEFAULT,
			ClientValues.DEFAULT
	);

	public static final Codec<SplitConfig> SERVER_CODEC = RecordCodecBuilder.create(inst -> inst.group(
			CommonValues.CODEC
					.forGetter(SplitConfig::common)
	).apply(inst, commonValues -> new SplitConfig(commonValues, null)));
	public static final Codec<SplitConfig> CLIENT_CODEC = RecordCodecBuilder.create(inst -> inst.group(
			CommonValues.CODEC
					.forGetter(SplitConfig::common),
			ClientValues.CODEC
					.forGetter(SplitConfig::client)
	).apply(inst, SplitConfig::new));


	public static StreamCodec<FriendlyByteBuf, SplitConfig> streamCodec(SplitConfig config) {
		return StreamCodec.composite(
				CommonValues.STREAM_CODEC, SplitConfig::common,
				common -> new SplitConfig(common, config.client));
	}


	public List<? extends Late> getLateValues() {
		return List.of(common.enchantmentOpinion.getFirst(), common.redBlocks, common.greenBiomes);
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

	public record CommonValues(int silly,
							   Pair<LateHolder<Enchantment>, Opinion> enchantmentOpinion,
							   LateHolderSet<Block> redBlocks,
							   LateHolderSet<Biome> greenBiomes,
							   TextColor color) {
		public static final Codec<Pair<LateHolder<Enchantment>, Opinion>> ENCHANTMENT_OPINION_CODEC = RecordCodecBuilder.create(inst -> inst.group(
				CommentedValue.codec(LateHolder.codec(Registries.ENCHANTMENT), "The enchantment you wish to provide an opinion on.").fieldOf("enchantment").forGetter(Pair::getFirst),
				CommentedValue.codec(Opinion.CODEC, "The opinion you have on the above enchantment.", "Can either be 'like' or 'dislike' (case-sensitive)").fieldOf("opinion").forGetter(Pair::getSecond)
		).apply(inst, Pair::of));

		public static final CommonValues DEFAULT = new CommonValues(
				69,
				Pair.of(LateHolder.create(Enchantments.FROST_WALKER), Opinion.LIKE),
				LateHolderSet.builder(Registries.BLOCK)
						.add(ResourceKey.create(Registries.BLOCK, ResourceLocation.withDefaultNamespace("netherrack")))
						.build(),
				LateHolderSet.builder(Registries.BIOME)
						.add(GREENS)
						.add(Biomes.BAMBOO_JUNGLE)
						.build(),
				TextColor.parseColor("#0095a8").getOrThrow()
		);
		public static final MapCodec<CommonValues> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
				DefaultFieldUtil.codecWithComments(Codec.INT, "silly", DEFAULT.silly(), "The value which makes this config very silly.")
						.forGetter(CommonValues::silly),
				DefaultFieldUtil.codecWithComments(ENCHANTMENT_OPINION_CODEC, "enchantment_opinion", DEFAULT.enchantmentOpinion(), "An enchantment that you either like or dislike.", "Note: Calico was not biased here.")
						.forGetter(CommonValues::enchantmentOpinion),
				DefaultFieldUtil.codecWithComments(
						LateHolderSet.codec(Registries.BLOCK),
						"red_blocks",
						DEFAULT.redBlocks(),
						"One block, two block, red block, blue block."
				).forGetter(CommonValues::redBlocks),
				DefaultFieldUtil.codecWithComments(
						LateHolderSet.codec(Registries.BIOME),
						"green_biomes",
						DEFAULT.greenBiomes(),
						"Biomes that are green",
						"This is an extra line to show how green they really are!"
				).forGetter(CommonValues::greenBiomes),
				DefaultFieldUtil.codecWithComments(
						TextColor.CODEC,
						"color",
						DEFAULT.color(),
						"This is a value that exists on both the client and server."
				).forGetter(CommonValues::color)
		).apply(inst, CommonValues::new));

		public static final StreamCodec<FriendlyByteBuf, CommonValues> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.INT, CommonValues::silly,
				LateHolder.streamCodec(Registries.ENCHANTMENT), config -> config.enchantmentOpinion().getFirst(),
				Opinion.STREAM_CODEC, config -> config.enchantmentOpinion().getSecond(),
				LateHolderSet.streamCodec(Registries.BLOCK), CommonValues::redBlocks,
				LateHolderSet.streamCodec(Registries.BIOME), CommonValues::greenBiomes,
				ByteBufCodecs.fromCodec(TextColor.CODEC), CommonValues::color,
				CommonValues::new);

		public CommonValues(int silly,
							LateHolder<Enchantment> enchantment,
							Opinion opinion,
							LateHolderSet<Block> redBlocks,
							LateHolderSet<Biome> greenBiomes,
							TextColor color) {
			this(silly, Pair.of(enchantment, opinion), redBlocks, greenBiomes, color);
		}
	}

	public record ClientValues(TextColor color) {
		public static final ClientValues DEFAULT = new ClientValues(TextColor.parseColor("#54bf6b").getOrThrow());

		public static final MapCodec<ClientValues> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
				DefaultFieldUtil.codecWithComments(TextColor.CODEC, "client_color", DEFAULT.color(), "This is a value that only exists on the client.")
						.forGetter(ClientValues::color)
		).apply(inst, ClientValues::new));
	}

	public static class Fixer {
		public static final DataFixer CLIENT = createFixer(true);
		public static final DataFixer SERVER = createFixer(false);

		private static DataFixer createFixer(boolean isClient) {
			DataFixerBuilder builder = new DataFixerBuilder(3);
			builder.addSchema(1, EmptySchema::new);
			Schema v2 = builder.addSchema(2, Schema::new);
			builder.addFixer(new V1ToV2FieldsFix(v2));
			Schema v3 = builder.addSchema(3, Schema::new);
			builder.addFixer(new V2ToV3FieldsFix(v3, isClient));
			return builder.build().fixer();
		}
	}

	/**
	 * Purely used for testing the fixer.
	 * You'll only need the fixer if you are implementing backwards compatibility with a previous config version.
	 */
	public static class PreviousVersionCodecs {
		public static final Codec<SplitConfig> V2 = RecordCodecBuilder.create(inst -> inst.group(
				DefaultFieldUtil.codecWithComments(
						Codec.INT,
						"silly",
						SERVER_DEFAULT.common().silly(),
						"The value which makes this config very silly."
				).forGetter(config -> config.common().silly()),
				DefaultFieldUtil.codecWithComments(
						LateHolder.codec(Registries.ENCHANTMENT),
						"liked_enchantment",
						SERVER_DEFAULT.common().enchantmentOpinion().getFirst(),
						"An enchantment you like.", "Note: Calico was not biased here."
				).forGetter(config -> SERVER_DEFAULT.common().enchantmentOpinion().getFirst()),
				DefaultFieldUtil.codecWithComments(
						LateHolderSet.codec(Registries.BLOCK),
						"blue_blocks",
						SERVER_DEFAULT.common().redBlocks(),
						"One block, two block, red block, blue block."
				).forGetter(config -> config.common().redBlocks()),
				DefaultFieldUtil.codecWithComments(
						LateHolderSet.codec(Registries.BIOME),
						"green_biomes",
						SERVER_DEFAULT.common().greenBiomes(),
						"Biomes that are green",
						"This is an extra line to show how green they really are!"
				).forGetter(config -> config.common().greenBiomes())
		).apply(inst, (t1, t2, t3, t4) ->
				new SplitConfig(new CommonValues(t1, Pair.of(t2, Opinion.LIKE), t3, t4, CommonValues.DEFAULT.color()), ClientValues.DEFAULT)));

		public static final Codec<SplitConfig> V1 = RecordCodecBuilder.create(inst -> inst.group(
				DefaultFieldUtil.codec(Codec.INT, "funny", SERVER_DEFAULT.common().silly()).forGetter(config -> config.common().silly()),
				DefaultFieldUtil.codec(LateHolderSet.codec(Registries.BIOME), "green_biomes", SERVER_DEFAULT.common().greenBiomes()).forGetter(config -> config.common().greenBiomes())
		).apply(inst, (t1, t2) ->
				new SplitConfig(new CommonValues(t1, CommonValues.DEFAULT.enchantmentOpinion(), CommonValues.DEFAULT.redBlocks(), t2, CommonValues.DEFAULT.color()), ClientValues.DEFAULT)));
	}
}
