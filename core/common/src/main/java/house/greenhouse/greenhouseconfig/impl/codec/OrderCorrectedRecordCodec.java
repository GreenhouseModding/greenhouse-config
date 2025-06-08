package house.greenhouse.greenhouseconfig.impl.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

/**
 * Moved to API package.
 * @see house.greenhouse.greenhouseconfig.api.OrderCorrectedRecordCodec
 */
@Deprecated(forRemoval = true)
public class OrderCorrectedRecordCodec {

	@Deprecated(forRemoval = true, since = "2.2.1+1.21.1")
	public static <T> Codec<T> wrap(Codec<T> codec) {
		return house.greenhouse.greenhouseconfig.api.OrderCorrectedRecordCodec.wrap(codec);
	}

	@Deprecated(forRemoval = true, since = "2.2.1+1.21.1")
	public static <T> MapCodec<T> wrap(MapCodec<T> codec) {
		return house.greenhouse.greenhouseconfig.api.OrderCorrectedRecordCodec.wrap(codec);
	}
}
