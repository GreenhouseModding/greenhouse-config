package house.greenhouse.greenhouseconfig.api;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import net.minecraft.util.Mth;

import java.util.List;
import java.util.stream.Stream;

/**
 * Accounts for a DFU bug where RecordCodecBuilder swaps the half-point at which members are encoded.
 * <p>
 * This should only ever be used on codecs with over 4 fields, which is where this bug is present.
 * @param <T> The type parameter of the RecordCodecBuilder.
 */
public class OrderCorrectedRecordCodec<T> extends MapCodec<T> {
	private final MapCodec<T> codec;

	private OrderCorrectedRecordCodec(MapCodec<T> codec) {
		this.codec = codec;
	}

	public static <T> Codec<T> wrap(Codec<T> codec) {
		return new OrderCorrectedRecordCodec<>(MapCodec.assumeMapUnsafe(codec)).codec();
	}

	public static <T> MapCodec<T> wrap(MapCodec<T> codec) {
		return new OrderCorrectedRecordCodec<>(codec);
	}

	@Override
	public <T1> DataResult<T> decode(DynamicOps<T1> ops, MapLike<T1> input) {
		return codec.decode(ops, input);
	}

	@Override
	public <T1> RecordBuilder<T1> encode(T input, DynamicOps<T1> ops, RecordBuilder<T1> prefix) {
		return correctEncoding(
				prefix,
				ops.getMap(codec.encode(input, ops, ops.mapBuilder()).build(ops.empty()).getOrThrow()).getOrThrow()
		);
	}

	@Override
	public <T1> Stream<T1> keys(DynamicOps<T1> ops) {
		return codec.keys(ops);
	}

	private static <T> RecordBuilder<T> correctEncoding(RecordBuilder<T> prefix, MapLike<T> newValues) {
		if (newValues.entries().count() > 4) {
			List<Pair<T, T>> elements = newValues.entries().toList();

			for (int i = Mth.ceil(elements.size() / 2.0F); i < elements.size(); ++i) {
				prefix.add(elements.get(i).getFirst(), elements.get(i).getSecond());
			}
			for (int i = 0; i < Mth.ceil(elements.size() / 2.0F); ++i) {
				prefix.add(elements.get(i).getFirst(), elements.get(i).getSecond());
			}
		} else {
			for (Pair<T, T> entry : newValues.entries().toList()) {
				prefix.add(entry.getFirst(), entry.getSecond());
			}
		}

		return prefix;
	}
}
